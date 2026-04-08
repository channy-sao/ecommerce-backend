package ecommerce_app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.dto.request.POSOrderItemRequest;
import ecommerce_app.dto.request.POSOrderRequest;
import ecommerce_app.dto.response.POSOrderItemResponse;
import ecommerce_app.dto.response.POSOrderResponse;
import ecommerce_app.entity.OrderItem;
import ecommerce_app.entity.Payment;
import ecommerce_app.entity.PaymentTransaction;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.Stock;
import ecommerce_app.entity.User;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.OrderMapper;
import ecommerce_app.mapper.OrderStatusHistoryMapper;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;
import ecommerce_app.dto.response.OrderStatusHistoryResponse;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.OrderStatusHistory;
import ecommerce_app.repository.OrderItemRepository;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.repository.OrderStatusHistoryRepository;
import ecommerce_app.repository.PaymentRepository;
import ecommerce_app.repository.PaymentTransactionRepository;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.repository.StockRepository;
import ecommerce_app.repository.UserRepository;
import ecommerce_app.service.AdminManageOrderService;
import ecommerce_app.specification.OrderSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
  private final StockRepository stockRepository;
  private final OrderItemRepository orderItemRepository;
  private final FinancialService financialService;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    OrderStatus currentStatus = order.getOrderStatus();

    validateTransition(currentStatus, newStatus);

    // 🔥 Update order
    order.setOrderStatus(newStatus);

    // 🔥 Save history
    OrderStatusHistory history =
        OrderStatusHistory.builder().order(order).status(newStatus).build();

    orderStatusHistoryRepository.save(history);
  }

  @Override
  @Transactional
  public POSOrderResponse createPOSOrder(POSOrderRequest request, Long staffUserId)
      throws JsonProcessingException {
    log.info("Creating POS order by staff: {}", staffUserId);

    User staff =
        userRepository
            .findById(staffUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

    // Create order
    Order order = new Order();
    order.setOrderNumber(generateOrderNumber());
    order.setOrderDate(LocalDateTime.now());
    order.setOrderStatus(OrderStatus.COMPLETED); // POS orders are completed immediately
    order.setPaymentStatus(PaymentStatus.PAID); // Payment collected immediately

    // Set payment method
    order.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));

    // Set customer info (optional)
    if (StringUtils.isNotBlank(request.getCustomerName())) {
      order.setShippingAddressSnapshot(
          JsonUtils.toJson(
              Map.of(
                  "customerName", request.getCustomerName(),
                  "customerEmail", request.getCustomerEmail(),
                  "customerPhone", request.getCustomerPhone())));
    }

    Order savedOrder = orderRepository.save(order);

    // Process items and calculate totals
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

      // Check stock
      Stock stock =
          stockRepository
              .findByProductId(product.getId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Stock not found for product: " + product.getName()));

      if (stock.getQuantity() < itemReq.getQuantity()) {
        throw new BadRequestException("Insufficient stock for product: " + product.getName());
      }

      // Deduct stock
      stock.setQuantity(stock.getQuantity() - itemReq.getQuantity());
      stockRepository.save(stock);

      BigDecimal unitPrice =
          itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : product.getPrice();
      BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
      subtotal = subtotal.add(itemTotal);

      OrderItem orderItem =
          OrderItem.builder()
              .order(savedOrder)
              .product(product)
              .quantity(itemReq.getQuantity())
              .originalPrice(unitPrice)
              .subtotal(itemTotal)
              .totalPrice(itemTotal)
              .build();

      orderItems.add(orderItem);
    }

    orderItemRepository.saveAll(orderItems);
    savedOrder.setOrderItems(orderItems);

    // Calculate totals
    BigDecimal discount =
        request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
    BigDecimal total = subtotal.subtract(discount);

    savedOrder.setSubtotalAmount(subtotal);
    savedOrder.setDiscountAmount(discount);
    savedOrder.setTotalAmount(total);
    orderRepository.save(savedOrder);

    // Create payment record
    Payment payment =
        Payment.builder()
            .order(savedOrder)
            .gateway(PaymentGateway.fromPaymentMethod(order.getPaymentMethod()))
            .amount(total)
            .currency("USD")
            .status(PaymentStatus.PAID)
            .paidAt(LocalDateTime.now())
            .gatewayReference("POS-" + savedOrder.getOrderNumber())
            .build();

    paymentRepository.save(payment);

    // Record transaction
    String staffName = staff.getFullName() != null ? staff.getFullName() : staff.getEmail();
    PaymentTransaction transaction =
        financialService.recordCashPayment(savedOrder, payment, staffUserId, staffName);

    log.info(
        "POS order created: {} with receipt: {}",
        savedOrder.getOrderNumber(),
        transaction.getReferenceNumber());

    return buildPOSResponse(savedOrder, transaction, staffName);
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

    return buildPOSResponse(order, transaction, null);
  }

  @Override
  @Transactional(readOnly = true)
  public List<POSOrderResponse> getTodayPOSOrders(LocalDate date, Long staffUserId) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);

    List<Order> orders =
        orderRepository.findByOrderDateBetweenAndCreatedBy(
            startOfDay,
            endOfDay,
            userRepository.findById(staffUserId).map(User::getEmail).orElse(null));

    return orders.stream()
        .map(
            order -> {
              PaymentTransaction transaction =
                  transactionRepository.findByOrderId(order.getId()).stream()
                      .findFirst()
                      .orElse(null);
              return buildPOSResponse(order, transaction, null);
            })
        .collect(Collectors.toList());
  }

  private String generateOrderNumber() {
    return "POS-"
        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        + "-"
        + (int) (Math.random() * 1000);
  }

  private POSOrderResponse buildPOSResponse(
      Order order, PaymentTransaction transaction, String cashierName) {
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
        .cashierName(
            cashierName != null
                ? cashierName
                : transaction != null ? transaction.getCashierName() : null)
        .items(items)
        .build();
  }

  @Transactional(readOnly = true)
  @Override
  public Page<OrderResponse> adminGetOrders(
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
        OrderSpecification.filter(orderStatus, paymentStatus, fromDate, toDate);
    Sort sort = Sort.by(sortDirection, sortBy);
    if (!isPaged) {
      final List<Order> productList = orderRepository.findAll(specification, sort);
      final List<OrderResponse> productResponseList =
          productList.stream().map(orderMapper::toSimpleResponse).toList();
      return new PageImpl<>(productResponseList);
    }
    // page start from 0
    final var pageable = PageRequest.of(page - 1, pageSize, sort);
    final Page<Order> productPage = orderRepository.findAll(specification, pageable);
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

    // Map order entity → detail DTO
    OrderDetailResponse response = OrderMapper.toDetailResponse(order);

    // Load status history (sorted by createdAt asc)
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
        PageRequest.of(
            page - 1, size, Sort.by(Sort.Direction.DESC, "orderDate") // Show newest first
            );

    List<PaymentMethod> methods;
    if (paymentMethod != null) {
      methods = List.of(paymentMethod);
    } else {
      methods = List.of(PaymentMethod.COD, PaymentMethod.CASH_IN_SHOP, PaymentMethod.CASH);
    }

    Page<Order> orders;

    if (includePaid) {
      // For collected history - ONLY PAID orders
      orders =
          orderRepository.findByPaymentMethodInAndPaymentStatusAndOrderStatusNot(
              methods,
              PaymentStatus.PAID, // Only PAID
              OrderStatus.CANCELLED,
              pageRequest);
    } else {
      // For pending collection - exclude PAID and CANCELLED
      orders =
          orderRepository.findByPaymentMethodInAndPaymentStatusNotAndOrderStatusNot(
              methods,
              PaymentStatus.PAID, // Exclude PAID
              OrderStatus.CANCELLED,
              pageRequest);
    }

    return orders.map(orderMapper::toSimpleResponse);
  }

  private List<PaymentMethod> getPaymentMethods(PaymentMethod paymentMethod) {
    if (paymentMethod != null) {
      return List.of(paymentMethod);
    }
    return List.of(PaymentMethod.COD, PaymentMethod.CASH_IN_SHOP, PaymentMethod.CASH);
  }

  private void validateTransition(OrderStatus from, OrderStatus to) {
    if (from == OrderStatus.COMPLETED) {
      throw new IllegalStateException("Completed order cannot be changed");
    }
  }
}
