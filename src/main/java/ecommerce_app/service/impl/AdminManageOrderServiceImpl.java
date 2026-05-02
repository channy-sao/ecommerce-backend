package ecommerce_app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import ecommerce_app.constant.enums.*;
import ecommerce_app.dto.request.POSOrderItemRequest;
import ecommerce_app.dto.request.POSOrderRequest;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.response.*;
import ecommerce_app.entity.*;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.OrderMapper;
import ecommerce_app.mapper.OrderStatusHistoryMapper;
import ecommerce_app.repository.*;
import ecommerce_app.service.AdminManageOrderService;
import ecommerce_app.service.StockManagementService;
import ecommerce_app.specification.OrderSpecification;
import ecommerce_app.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminManageOrderServiceImpl implements AdminManageOrderService {

  private final OrderRepository orderRepository;
  private final OrderStatusHistoryRepository orderStatusHistoryRepository;
  private final OrderStatusHistoryMapper orderStatusHistoryMapper;
  private final OrderMapper orderMapper;
  private final UserRepository userRepository;
  private final PaymentTransactionRepository transactionRepository;
  private final ProductRepository productRepository;
  private final PaymentRepository paymentRepository;
  private final OrderItemRepository orderItemRepository;
  private final FinancialService financialService;
  private final OrderNumberGenerator orderNumberGenerator;

  // ✅ REPLACED StockRepository with these two
  private final ProductVariantRepository variantRepository;
  private final StockManagementService stockManagementService;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    OrderStatus currentStatus = order.getOrderStatus();
    validateTransition(currentStatus, newStatus);

    order.setOrderStatus(newStatus);
    orderRepository.save(order); // ✅ Save the order!

    OrderStatusHistory history =
        OrderStatusHistory.builder().order(order).status(newStatus).build();
    orderStatusHistoryRepository.save(history);

    log.info("Order #{} status changed: {} → {}", order.getOrderNumber(), currentStatus, newStatus);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public POSOrderResponse createPOSOrder(POSOrderRequest request, Long staffUserId)
      throws JsonProcessingException {
    log.info("Creating POS order by staff: {}", staffUserId);

    User staff =
        userRepository
            .findById(staffUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

    // Create order
    Order order = new Order();
    order.setOrderNumber(orderNumberGenerator.generatePOSOrderNumber());
    order.setOrderDate(LocalDateTime.now());
    order.setOrderStatus(OrderStatus.COMPLETED);
    order.setPaymentStatus(PaymentStatus.PAID);
    order.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
    order.setUser(staff);
    order.setSubtotalAmount(BigDecimal.ZERO);
    order.setDiscountAmount(BigDecimal.ZERO);
    order.setShippingCost(BigDecimal.ZERO);
    order.setTotalAmount(BigDecimal.ZERO);
    order.setShippingMethod(ShippingMethod.STANDARD);
    order.setCart(null);

    // Set customer info
    if (StringUtils.isNotBlank(request.getCustomerName())) {
      Map<String, String> customerInfo = new HashMap<>();
      customerInfo.put("customerName", truncate(request.getCustomerName(), 255));
      customerInfo.put("customerEmail", truncate(request.getCustomerEmail(), 255));
      customerInfo.put("customerPhone", truncate(request.getCustomerPhone(), 50));

      String snapshot = JsonUtils.toJson(customerInfo);
      if (snapshot != null && snapshot.length() > 1000) {
        snapshot = snapshot.substring(0, 1000);
      }
      order.setShippingAddressSnapshot(snapshot);
    } else {
      order.setShippingAddressSnapshot("{}");
    }

    if (StringUtils.isNotBlank(request.getNotes())) {
      order.setNotes(truncate(request.getNotes(), 500));
    }

    Order savedOrder = orderRepository.save(order);

    // Process items
    BigDecimal subtotal = BigDecimal.ZERO;
    List<OrderItem> orderItems = new ArrayList<>();

    for (POSOrderItemRequest itemReq : request.getItems()) {
      Product product =
          productRepository
              .findById(itemReq.getProductId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Product not found: " + itemReq.getProductId()));

      // ✅ Resolve variant - use provided variantId or default variant
      ProductVariant variant = resolveVariant(product, itemReq.getVariantId());

      // ✅ Validate stock on variant
      if (variant.getStockQuantity() < itemReq.getQuantity()) {
        throw new BadRequestException(
            String.format(
                "Insufficient stock for '%s' (%s). Available: %d, Requested: %d",
                product.getName(),
                variant.getSku(),
                variant.getStockQuantity(),
                itemReq.getQuantity()));
      }

      // ✅ Deduct stock via StockManagementService
      stockManagementService.adjustStock(
          StockAdjustmentRequest.builder()
              .productId(product.getId())
              .variantId(variant.getId())
              .movementType(StockMovementType.OUT)
              .quantity(itemReq.getQuantity())
              .referenceType("POS_ORDER")
              .referenceId(savedOrder.getId())
              .note("POS Order #" + savedOrder.getOrderNumber())
              .build(),
          staffUserId);

      BigDecimal unitPrice =
          itemReq.getUnitPrice() != null
              ? itemReq.getUnitPrice()
              : variant.getPrice() != null ? variant.getPrice() : product.getPrice();

      BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
      subtotal = subtotal.add(itemTotal);

      OrderItem orderItem =
          OrderItem.builder()
              .order(savedOrder)
              .product(product)
              .variant(variant) // ✅ Carry variant reference
              .quantity(itemReq.getQuantity())
              .originalPrice(unitPrice)
              .subtotal(itemTotal)
              .totalPrice(itemTotal)
              .cart(null)
              .discountAmount(BigDecimal.ZERO)
              .build();

      orderItems.add(orderItem);
    }

    orderItemRepository.saveAll(orderItems);

    if (savedOrder.getOrderItems() == null) {
      savedOrder.setOrderItems(new ArrayList<>());
    }
    savedOrder.getOrderItems().addAll(orderItems);

    // Calculate totals
    BigDecimal discount =
        request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
    BigDecimal total = subtotal.subtract(discount);

    savedOrder.setSubtotalAmount(subtotal);
    savedOrder.setDiscountAmount(discount);
    savedOrder.setTotalAmount(total);

    Order finalOrder = orderRepository.save(savedOrder);

    // Cash change logic
    BigDecimal cashReceived = null;
    BigDecimal changeAmount = null;
    String paymentNote = null;

    if ("CASH".equals(request.getPaymentMethod())
        || "CASH_IN_SHOP".equals(request.getPaymentMethod())) {
      if (request.getCashReceived() != null) {
        if (request.getCashReceived().compareTo(total) < 0) {
          throw new BadRequestException(
              "Cash received ("
                  + request.getCashReceived()
                  + ") is less than total ("
                  + total
                  + ")");
        }
        cashReceived = request.getCashReceived();
        changeAmount = cashReceived.subtract(total);
        paymentNote = "Cash received: " + cashReceived + ", Change: " + changeAmount;
      }
    }

    // Create payment record
    Payment payment =
        Payment.builder()
            .order(finalOrder)
            .gateway(PaymentGateway.fromPaymentMethod(finalOrder.getPaymentMethod()))
            .amount(total)
            .currency("USD")
            .status(PaymentStatus.PAID)
            .paidAt(LocalDateTime.now())
            .gatewayReference("POS-" + finalOrder.getOrderNumber())
            .createdAt(LocalDateTime.now())
            .build();

    paymentRepository.save(payment);

    // Record transaction
    String staffName = staff.getFullName() != null ? staff.getFullName() : staff.getEmail();
    PaymentTransaction transaction =
        financialService.recordCashPayment(
            finalOrder, payment, staffUserId, staffName, total, paymentNote);

    log.info(
        "POS order created: {} with receipt: {}",
        finalOrder.getOrderNumber(),
        transaction.getReferenceNumber());

    return buildPOSResponse(finalOrder, transaction, staffName, cashReceived, changeAmount);
  }

  /**
   * ✅ Resolve variant for POS item. If variantId provided, use it. Otherwise, use default variant.
   */
  private ProductVariant resolveVariant(Product product, Long variantId) {
    if (variantId != null) {
      return variantRepository
          .findById(variantId)
          .orElseThrow(() -> new ResourceNotFoundException("Variant", variantId));
    }
    // Simple product - use default variant
    return variantRepository
        .findByProductIdAndIsDefaultTrue(product.getId())
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Default variant for product: " + product.getName()));
  }

  private String truncate(String value, int maxLength) {
    if (value == null) return null;
    if (value.length() <= maxLength) return value;
    return value.substring(0, maxLength);
  }

  @Override
  @Transactional(readOnly = true)
  public POSOrderResponse getPOSOrder(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

    PaymentTransaction transaction =
        transactionRepository.findByOrderId(orderId).stream().findFirst().orElse(null);

    Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
    BigDecimal cashReceived = payment != null ? payment.getCashReceived() : null;
    BigDecimal changeAmount = payment != null ? payment.getChangeAmount() : null;

    return buildPOSResponse(order, transaction, null, cashReceived, changeAmount);
  }

  @Override
  @Transactional(readOnly = true)
  public List<POSOrderResponse> getTodayPOSOrders(LocalDate date, Long staffUserId) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);

    List<Order> orders =
        orderRepository.findByOrderDateBetweenAndCreatedByWithItems(
            startOfDay, endOfDay, staffUserId);

    if (orders.isEmpty()) return List.of();

    List<Long> orderIds = orders.stream().map(Order::getId).toList();
    Map<Long, PaymentTransaction> transactionByOrderId =
        transactionRepository.findByOrderIdIn(orderIds).stream()
            .collect(Collectors.toMap(t -> t.getOrder().getId(), Function.identity(), (a, b) -> a));

    return orders.stream()
        .map(order -> buildPOSResponse(order, transactionByOrderId.get(order.getId())))
        .collect(Collectors.toList());
  }

  private String generateOrderNumber() {
    return "POS-"
        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        + "-"
        + (int) (Math.random() * 1000);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<OrderResponse> adminGetOrders(
      String orderNumber,
      OrderStatus orderStatus,
      PaymentStatus paymentStatus,
      LocalDate fromDate,
      LocalDate toDate,
      boolean isPaged,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction sortDirection) {

    Specification<Order> specification =
        OrderSpecification.filter(orderNumber, orderStatus, paymentStatus, fromDate, toDate);
    Sort sort = Sort.by(sortDirection, sortBy);

    if (!isPaged) {
      List<Order> productList = orderRepository.findAll(specification, sort);
      List<OrderResponse> productResponseList =
          productList.stream().map(orderMapper::toSimpleResponse).toList();
      return new PageImpl<>(productResponseList);
    }

    PageRequest pageable = PageRequest.of(page - 1, pageSize, sort);
    Page<Order> productPage = orderRepository.findAll(specification, pageable);
    return productPage.map(orderMapper::toSimpleResponse);
  }

  @Transactional(readOnly = true)
  @Override
  public OrderDetailResponse getOrderDetailForAdmin(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found with ID: " + orderId));

    OrderDetailResponse response = orderMapper.toDetailResponse(order);

    List<OrderStatusHistoryResponse> histories =
        orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
            .map(orderStatusHistoryMapper::toResponse)
            .toList();

    response.setStatusHistories(histories);
    return response;
  }

  @Transactional(readOnly = true)
  @Override
  public Page<OrderResponse> getOrdersReadyForCollection(
      PaymentMethod paymentMethod, int page, int size, boolean includePaid) {

    PageRequest pageRequest =
        PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "orderDate"));

    List<PaymentMethod> methods = getPaymentMethods(paymentMethod);

    Page<Order> orders;
    if (includePaid) {
      orders =
          orderRepository.findByPaymentMethodInAndPaymentStatusAndOrderStatusNot(
              methods, PaymentStatus.PAID, OrderStatus.CANCELLED, pageRequest);
    } else {
      orders =
          orderRepository.findByPaymentMethodInAndPaymentStatusNotAndOrderStatusNot(
              methods, PaymentStatus.PAID, OrderStatus.CANCELLED, pageRequest);
    }

    return orders.map(orderMapper::toSimpleResponse);
  }

  private List<PaymentMethod> getPaymentMethods(PaymentMethod paymentMethod) {
    if (paymentMethod != null) {
      return List.of(paymentMethod);
    }
    return List.of(PaymentMethod.COD, PaymentMethod.CASH_IN_SHOP, PaymentMethod.CASH);
  }

  private POSOrderResponse buildPOSResponse(
      Order order,
      PaymentTransaction transaction,
      String staffName,
      BigDecimal cashReceived,
      BigDecimal changeAmount) {

    String cashierName = staffName;
    if (cashierName == null) {
      cashierName =
          (transaction != null && transaction.getCashierName() != null)
              ? transaction.getCashierName()
              : (order.getUser() != null ? order.getUser().getFullName() : null);
    }

    List<POSOrderItemResponse> items =
        order.getOrderItems().stream()
            .map(
                item ->
                    POSOrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getOriginalPrice())
                        .total(item.getTotalPrice())
                        .build())
            .collect(Collectors.toList());

    return POSOrderResponse.builder()
        .orderId(order.getId())
        .orderNumber(order.getOrderNumber())
        .orderDate(order.getOrderDate())
        .subtotal(order.getSubtotalAmount())
        .discountAmount(order.getDiscountAmount())
        .totalAmount(order.getTotalAmount())
        .paymentMethod(order.getPaymentMethod().name())
        .paymentStatus(order.getPaymentStatus().name())
        .orderStatus(order.getOrderStatus().name())
        .receiptNumber(transaction != null ? transaction.getReferenceNumber() : null)
        .cashierName(cashierName)
        .cashReceived(cashReceived)
        .changeAmount(changeAmount)
        .items(items)
        .build();
  }

  private POSOrderResponse buildPOSResponse(Order order, PaymentTransaction transaction) {
    return buildPOSResponse(order, transaction, null, null, null);
  }

  private void validateTransition(OrderStatus from, OrderStatus to) {
    if (from == OrderStatus.COMPLETED) {
      throw new IllegalStateException("Completed order cannot be changed");
    }
  }
}
