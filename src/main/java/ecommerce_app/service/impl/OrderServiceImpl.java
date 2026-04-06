package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.CartStatus;
import ecommerce_app.constant.enums.NotificationType;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.core.SimpleTry;
import ecommerce_app.dto.CheckoutSummary;
import ecommerce_app.dto.request.ApplyCouponRequest;
import ecommerce_app.dto.request.CheckoutRequest;
import ecommerce_app.dto.request.InitiatePaymentRequest;
import ecommerce_app.dto.request.NotificationRequest;
import ecommerce_app.dto.response.ApplyCouponResponse;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;
import ecommerce_app.entity.Address;
import ecommerce_app.entity.Cart;
import ecommerce_app.entity.CartItem;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.OrderItem;
import ecommerce_app.entity.OrderStatusHistory;
import ecommerce_app.entity.Payment;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.Promotion;
import ecommerce_app.entity.PromotionUsage;
import ecommerce_app.entity.Stock;
import ecommerce_app.entity.User;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.OrderMapper;
import ecommerce_app.repository.AddressRepository;
import ecommerce_app.repository.CartRepository;
import ecommerce_app.repository.OrderItemRepository;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.repository.OrderStatusHistoryRepository;
import ecommerce_app.repository.PaymentRepository;
import ecommerce_app.repository.PromotionUsageRepository;
import ecommerce_app.repository.StockRepository;
import ecommerce_app.repository.UserRepository;
import ecommerce_app.service.CouponService;
import ecommerce_app.service.OrderService;
import ecommerce_app.service.PaymentService;
import ecommerce_app.service.facade.PromotionFacade;
import ecommerce_app.util.JsonUtils;
import ecommerce_app.util.SimpleShippingCalculator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static ecommerce_app.constant.enums.PaymentGateway.BAKONG;

@Service
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

  // Payment methods that should auto-initiate after checkout
  private static final Set<PaymentMethod> AUTO_INITIATE_METHODS =
      Set.of(PaymentMethod.COD, PaymentMethod.CASH_IN_SHOP);

  private final OrderItemRepository orderItemRepository;
  private final PromotionUsageRepository promotionUsageRepository;
  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final AddressRepository addressRepository;
  private final PaymentRepository paymentRepository;
  private final PromotionFacade promotionFacade;
  private final SimpleShippingCalculator shippingCalculator;
  private final UserRepository userRepository;
  private final OrderStatusHistoryRepository orderStatusHistoryRepository;
  private final OrderMapper orderMapper;
  private final OrderNumberGenerator orderNumberGenerator;
  private final StockRepository stockRepository;
  private final CouponService couponService;
  private final NotificationService notificationService;
  private final PaymentService paymentService;

  // Use @Lazy on PaymentService to avoid circular dependency
  // OrderService → PaymentService → OrderRepository → fine
  // but PaymentService also uses OrderService indirectly via strategies
  public OrderServiceImpl(
      OrderItemRepository orderItemRepository,
      PromotionUsageRepository promotionUsageRepository,
      OrderRepository orderRepository,
      CartRepository cartRepository,
      AddressRepository addressRepository,
      PaymentRepository paymentRepository,
      PromotionFacade promotionFacade,
      SimpleShippingCalculator shippingCalculator,
      UserRepository userRepository,
      OrderStatusHistoryRepository orderStatusHistoryRepository,
      OrderMapper orderMapper,
      OrderNumberGenerator orderNumberGenerator,
      StockRepository stockRepository,
      CouponService couponService,
      NotificationService notificationService,
      @Lazy PaymentService paymentService) {
    this.orderItemRepository = orderItemRepository;
    this.promotionUsageRepository = promotionUsageRepository;
    this.orderRepository = orderRepository;
    this.cartRepository = cartRepository;
    this.addressRepository = addressRepository;
    this.paymentRepository = paymentRepository;
    this.promotionFacade = promotionFacade;
    this.shippingCalculator = shippingCalculator;
    this.userRepository = userRepository;
    this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    this.orderMapper = orderMapper;
    this.orderNumberGenerator = orderNumberGenerator;
    this.stockRepository = stockRepository;
    this.couponService = couponService;
    this.notificationService = notificationService;
    this.paymentService = paymentService;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public OrderResponse checkout(CheckoutRequest checkoutRequest, Long userId) {
    log.info("Checkout request received: {}", checkoutRequest);

    User currentUser = getUserById(userId);
    Address shippingAddress = getShippingAddress(checkoutRequest, currentUser);
    Cart cart = retrieveCart(currentUser.getId());
    validateCart(cart);

    BigDecimal shippingCost = calculateShippingCost(checkoutRequest, cart, shippingAddress);
    CheckoutSummary checkoutSummary =
        calculateCheckoutSummary(cart, checkoutRequest.getPromotionCode());
    applyCouponToSummary(checkoutSummary, checkoutRequest.getCouponCode(), userId);

    if (isFreeShippingPromotion(checkoutSummary)) {
      shippingCost = BigDecimal.ZERO;
      checkoutSummary.setShippingDiscount(checkoutSummary.getShippingCost());
      checkoutSummary.setFreeShipping(true);
    }

    final BigDecimal subtotalAmount = checkoutSummary.getSubtotal();
    final BigDecimal discountAmount = checkoutSummary.getTotalDiscount();
    final BigDecimal couponDiscount = checkoutSummary.getCouponDiscount();
    final BigDecimal shippingAmount = shippingCost;
    final BigDecimal totalAmount =
        checkoutSummary
            .getFinalTotal()
            .subtract(couponDiscount)
            .add(shippingAmount)
            .max(BigDecimal.ZERO);

    log.info(
        "Checkout totals - Subtotal: {}, Discount: {}, Shipping: {}, Total: {}",
        subtotalAmount,
        discountAmount,
        shippingAmount,
        totalAmount);

    Order order =
        createOrder(
            currentUser,
            cart,
            checkoutRequest,
            shippingAddress,
            subtotalAmount,
            discountAmount,
            shippingAmount,
            totalAmount,
            couponDiscount,
            checkoutRequest.getCouponCode());

    String orderNumber = orderNumberGenerator.generateOrderNumber();
    order.setOrderNumber(orderNumber);
    log.debug("Generated order number: {}", orderNumber);

    Order savedOrder = orderRepository.save(order);
    saveOrderStatusHistory(savedOrder, savedOrder.getOrderStatus());
    log.info("Order created with ID: {}", savedOrder.getId());

    List<OrderItem> orderItems = createOrderItems(cart, savedOrder, checkoutSummary);
    orderItemRepository.saveAll(orderItems);
    log.info("Created {} order items", orderItems.size());

    deductStock(cart);
    log.info("Stock deducted for order: {}", savedOrder.getOrderNumber());

    recordPromotionUsageIfApplicable(checkoutSummary, savedOrder, currentUser);
    recordCouponUsageIfApplicable(checkoutSummary, savedOrder, currentUser);
    updateCartStatus(cart);

    // Send order placed notification
    sendOrderPlacedNotification(savedOrder, currentUser);

    // Auto-initiate payment for COD and CASH_IN_SHOP
    handlePaymentAfterCheckout(savedOrder, userId);

    // Re-fetch order to get latest status after auto-initiate
    Order finalOrder = orderRepository.findById(savedOrder.getId()).orElse(savedOrder);

    return orderMapper.toCheckoutResponse(finalOrder);
  }

  // ─── Auto-initiate payment ────────────────────────────────────────────────

  /**
   * Automatically initiates payment for COD and CASH_IN_SHOP orders right after checkout. This
   * moves the order from PENDING → CONFIRMED immediately. BAKONG/QR_CODE orders are NOT
   * auto-initiated — customer must pay via QR first.
   */
  // Fix in OrderServiceImpl.java - handlePaymentAfterCheckout method
  private void handlePaymentAfterCheckout(Order order, Long userId) {
    PaymentMethod method = order.getPaymentMethod();

    // Step 1: Always create payment (your strategy will handle behavior)
    InitiatePaymentRequest request = new InitiatePaymentRequest();
    request.setOrderId(order.getId());
    request.setGateway(PaymentGateway.fromPaymentMethod(method));

    paymentService.initiate(request, userId);

    // Step 2: Control ORDER + PAYMENT status (IMPORTANT)
    // IMPORTANT: Only set status if not already set by paymentService.initiate
    switch (method) {
      case COD -> {
        // Order should be CONFIRMED immediately for COD
        if (order.getOrderStatus() == OrderStatus.PENDING) {
          order.setOrderStatus(OrderStatus.CONFIRMED);
        }
        order.setPaymentStatus(PaymentStatus.PENDING);
      }
      case CASH_IN_SHOP -> {
        // Order is ready for pickup immediately
        if (order.getOrderStatus() == OrderStatus.PENDING) {
          order.setOrderStatus(OrderStatus.READY_FOR_PICKUP);
        }
        order.setPaymentStatus(PaymentStatus.PENDING);
      }
      case QR_CODE -> {
        // Keep PENDING until payment is confirmed via webhook
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
      }
      default -> throw new IllegalStateException("Unsupported payment method: " + method);
    }

    orderRepository.save(order);
    saveOrderStatusHistory(order, order.getOrderStatus());
  }

  // ─── Cancel order ─────────────────────────────────────────────────────────

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancelOrder(Long orderId, Long userId, String reason) {
    log.info("Cancel request for order ID: {} by user ID: {}", orderId, userId);

    Order order =
        orderRepository
            .findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

    validateCancellable(order);

    order.setOrderStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);

    saveOrderStatusHistory(order, OrderStatus.CANCELLED);
    restoreStock(order);
    log.info("Stock restored for order: {}", order.getOrderNumber());

    sendOrderCancelledNotification(order, order.getUser());
    log.info("Order {} cancelled successfully", order.getOrderNumber());
  }

  private void validateCancellable(Order order) {
    if (order.getOrderStatus() == OrderStatus.SHIPPED
        || order.getOrderStatus() == OrderStatus.DELIVERED
        || order.getOrderStatus() == OrderStatus.CANCELLED) {
      throw new BadRequestException(
          "Order #"
              + order.getOrderNumber()
              + " cannot be cancelled. Status: "
              + order.getOrderStatus());
    }
  }

  private void restoreStock(Order order) {
    for (OrderItem item : order.getOrderItems()) {
      Stock stock =
          stockRepository
              .findByProductId(item.getProduct().getId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Stock not found for product: " + item.getProduct().getName()));
      stock.setQuantity(stock.getQuantity() + item.getQuantity());
      stockRepository.save(stock);
      log.info(
          "Restored {} units to product '{}'", item.getQuantity(), item.getProduct().getName());
    }
  }

  // ─── Queries ──────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  @Override
  public Page<OrderResponse> getOrders(Long userId, int page, int pageSize) {
    log.info("Fetching orders for user ID: {}", userId);
    PageRequest pageRequest =
        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "orderDate"));
    return orderRepository.findByUserId(userId, pageRequest).map(orderMapper::toSimpleResponse);
  }

  @Transactional(readOnly = true)
  @Override
  public OrderDetailResponse getOrderDetails(Long orderId, Long userId) {
    log.info("Fetching order details for order ID: {}, user ID: {}", orderId, userId);
    Order order =
        orderRepository
            .findByIdAndUserId(orderId, userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    return OrderMapper.toDetailResponse(order);
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  private User getUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
  }

  private Cart retrieveCart(Long userId) {
    return cartRepository
        .findByUserIdAndStatus(userId, CartStatus.ACTIVE)
        .orElseThrow(
            () -> new ResourceNotFoundException("Active cart not found for user ID: " + userId));
  }

  private void validateCart(Cart cart) {
    if (CollectionUtils.isEmpty(cart.getCartItems())) {
      throw new IllegalStateException("Cannot checkout an empty cart.");
    }
  }

  private BigDecimal calculateShippingCost(
      CheckoutRequest checkoutRequest, Cart cart, Address address) {
    return shippingCalculator.calculateShippingCost(
        checkoutRequest.getShippingMethod(), cart, address);
  }

  private CheckoutSummary calculateCheckoutSummary(Cart cart, String promotionCode) {
    CheckoutSummary summary = new CheckoutSummary();
    BigDecimal subtotal = calculateSubtotal(cart);
    summary.setSubtotal(subtotal);
    summary.setTotalDiscount(BigDecimal.ZERO);
    summary.setShippingDiscount(BigDecimal.ZERO);
    summary.setFreeShipping(false);

    if (StringUtils.isNotBlank(promotionCode)) {
      applyPromotionToSummary(summary, cart, promotionCode);
    } else {
      applyAutoPromotionsToSummary(summary, cart);
    }

    BigDecimal finalTotal = calculateFinalTotal(summary);
    summary.setFinalTotal(finalTotal);
    return summary;
  }

  private BigDecimal calculateSubtotal(Cart cart) {
    return cart.getCartItems().stream()
        .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private void applyPromotionToSummary(CheckoutSummary summary, Cart cart, String promotionCode) {
    try {
      Promotion promotion = promotionFacade.getPromotionByCode(promotionCode);
      if (promotion == null) {
        summary.setPromotionError("Promotion code not found");
        return;
      }
      if (!promotionFacade.isPromotionActive(promotion)) {
        summary.setPromotionError("Promotion is not active or expired");
        return;
      }
      if (promotion.getMaxUsage() != null
          && promotion.getUsages() != null
          && promotion.getUsages().size() >= promotion.getMaxUsage()) {
        summary.setPromotionError("Promotion usage limit reached");
        return;
      }
      if (promotion.getMinPurchaseAmount() != null
          && summary.getSubtotal().compareTo(promotion.getMinPurchaseAmount()) < 0) {
        summary.setPromotionError(
            "Minimum purchase of $" + promotion.getMinPurchaseAmount() + " required");
        return;
      }
      if (promotion.getDiscountType() == PromotionType.FREE_SHIPPING) {
        summary.setAppliedPromotion(promotion);
        summary.setTotalDiscount(BigDecimal.ZERO);
        return;
      }

      summary.setAppliedPromotion(promotion);
      BigDecimal totalDiscount = BigDecimal.ZERO;
      Map<Long, BigDecimal> itemDiscounts = new HashMap<>();
      for (CartItem cartItem : cart.getCartItems()) {
        Product product = cartItem.getProduct();
        if (promotionFacade.isProductEligibleForPromotion(product, promotion)) {
          try {
            BigDecimal itemDiscount =
                promotionFacade.calculateDiscount(promotion, product, cartItem.getQuantity());
            if (itemDiscount != null && itemDiscount.compareTo(BigDecimal.ZERO) > 0) {
              itemDiscount =
                  itemDiscount.min(
                      product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
              itemDiscount = itemDiscount.min(summary.getSubtotal());
              itemDiscounts.put(product.getId(), itemDiscount);
              totalDiscount = totalDiscount.add(itemDiscount);
            }
          } catch (Exception e) {
            log.warn(
                "Failed to calculate discount for product {}: {}", product.getId(), e.getMessage());
          }
        }
      }
      if (totalDiscount.compareTo(summary.getSubtotal()) > 0) totalDiscount = summary.getSubtotal();
      summary.setItemDiscounts(itemDiscounts);
      summary.setTotalDiscount(totalDiscount);
      summary.setPromotionError(null);
    } catch (Exception e) {
      log.error("Error applying promotion {}: {}", promotionCode, e.getMessage(), e);
      summary.setPromotionError("Error applying promotion: " + e.getMessage());
    }
  }

  private void applyAutoPromotionsToSummary(CheckoutSummary summary, Cart cart) {
    try {
      BigDecimal totalDiscount = BigDecimal.ZERO;
      Map<Long, BigDecimal> itemDiscounts = new HashMap<>();
      for (CartItem cartItem : cart.getCartItems()) {
        final var product = cartItem.getProduct();
        var promotion =
            promotionFacade.getAvailablePromotion(product).stream().findFirst().orElse(null);
        if (promotion != null) {
          summary.setAppliedPromotion(promotion);
          BigDecimal itemDiscount =
              promotionFacade.calculateDiscount(promotion, product, cartItem.getQuantity());
          if (itemDiscount != null && itemDiscount.compareTo(BigDecimal.ZERO) > 0) {
            itemDiscount =
                itemDiscount.min(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            itemDiscount = itemDiscount.min(summary.getSubtotal());
            itemDiscounts.put(product.getId(), itemDiscount);
            totalDiscount = totalDiscount.add(itemDiscount);
          }
          if (totalDiscount.compareTo(summary.getSubtotal()) > 0)
            totalDiscount = summary.getSubtotal();
          summary.setItemDiscounts(itemDiscounts);
          summary.setTotalDiscount(totalDiscount);
          summary.setPromotionError(null);
        }
      }
    } catch (Exception e) {
      log.error("Error applying auto promotions: {}", e.getMessage(), e);
      summary.setPromotionError("Error applying promotion: " + e.getMessage());
    }
  }

  private BigDecimal calculateFinalTotal(CheckoutSummary summary) {
    BigDecimal subtotal = summary.getSubtotal() != null ? summary.getSubtotal() : BigDecimal.ZERO;
    BigDecimal discount =
        summary.getTotalDiscount() != null ? summary.getTotalDiscount() : BigDecimal.ZERO;
    return subtotal.subtract(discount.min(subtotal));
  }

  private boolean isFreeShippingPromotion(CheckoutSummary checkoutSummary) {
    return checkoutSummary.getAppliedPromotion() != null
        && checkoutSummary.getAppliedPromotion().getDiscountType() == PromotionType.FREE_SHIPPING;
  }

  private void saveOrderStatusHistory(Order order, OrderStatus status) {
    orderStatusHistoryRepository.save(
        OrderStatusHistory.builder().order(order).status(status).build());
  }

  private Order createOrder(
      User user,
      Cart cart,
      CheckoutRequest checkoutRequest,
      Address shippingAddress,
      BigDecimal subtotalAmount,
      BigDecimal discountAmount,
      BigDecimal shippingCost,
      BigDecimal totalAmount,
      BigDecimal couponDiscount,
      String couponCode) {
    return Order.builder()
        .user(user)
        .cart(cart)
        .subtotalAmount(subtotalAmount)
        .discountAmount(discountAmount)
        .promotionCode(checkoutRequest.getPromotionCode())
        .couponCode(checkoutRequest.getCouponCode())
        .couponDiscount(couponDiscount)
        .shippingCost(shippingCost)
        .shippingAddressSnapshot(getAddressSnapshot(shippingAddress))
        .shippingMethod(checkoutRequest.getShippingMethod())
        .totalAmount(totalAmount)
        .orderStatus(OrderStatus.PENDING)
        .paymentStatus(PaymentStatus.PENDING)
        .paymentMethod(checkoutRequest.getPaymentMethod())
        .orderDate(LocalDateTime.now())
        .build();
  }

  private String getAddressSnapshot(Address address) {
    return SimpleTry.ofChecked(() -> JsonUtils.toJson(address), null);
  }

  private List<OrderItem> createOrderItems(Cart cart, Order order, CheckoutSummary summary) {
    List<OrderItem> orderItems = new ArrayList<>();
    for (CartItem cartItem : cart.getCartItems()) {
      Product product = cartItem.getProduct();
      int quantity = cartItem.getQuantity();
      BigDecimal unitPrice = product.getPrice();
      BigDecimal discountAmount = getItemDiscount(summary, product.getId(), quantity, unitPrice);
      BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
      BigDecimal totalPrice = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);
      orderItems.add(
          OrderItem.builder()
              .product(product)
              .quantity(quantity)
              .originalPrice(unitPrice)
              .subtotal(subtotal)
              .discountAmount(discountAmount)
              .totalPrice(totalPrice)
              .promotionCode(
                  summary.getAppliedPromotion() != null
                      ? summary.getAppliedPromotion().getCode()
                      : null)
              .cart(cart)
              .order(order)
              .build());
    }
    return orderItems;
  }

  private BigDecimal getItemDiscount(
      CheckoutSummary summary, Long productId, int quantity, BigDecimal unitPrice) {
    BigDecimal discountAmount = BigDecimal.ZERO;
    if (summary.getItemDiscounts() != null) {
      discountAmount = summary.getItemDiscounts().getOrDefault(productId, BigDecimal.ZERO);
    }
    return discountAmount.min(unitPrice.multiply(BigDecimal.valueOf(quantity)));
  }

  private void recordPromotionUsageIfApplicable(CheckoutSummary summary, Order order, User user) {
    if (summary.getAppliedPromotion() != null) {
      promotionUsageRepository.save(
          PromotionUsage.builder()
              .promotion(summary.getAppliedPromotion())
              .order(order)
              .user(user)
              .usedAt(LocalDateTime.now())
              .discountAmount(
                  summary.getTotalDiscount() != null ? summary.getTotalDiscount() : BigDecimal.ZERO)
              .build());
    }
  }

  private void updateCartStatus(Cart cart) {
    cart.setStatus(CartStatus.CHECKED_OUT);
    cartRepository.save(cart);
    log.info("Updated cart {} status to CHECKED_OUT", cart.getId());
  }

  private void deductStock(Cart cart) {
    for (CartItem cartItem : cart.getCartItems()) {
      Product product = cartItem.getProduct();
      int orderedQuantity = cartItem.getQuantity();
      Stock stock =
          stockRepository
              .findByProductId(product.getId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Stock not found for product: " + product.getName()));
      int newQuantity = stock.getQuantity() - orderedQuantity;
      if (newQuantity < 0) {
        throw new IllegalStateException(
            String.format(
                "Insufficient stock for '%s'. Available: %d, Requested: %d",
                product.getName(), stock.getQuantity(), orderedQuantity));
      }
      stock.setQuantity(newQuantity);
      stockRepository.save(stock);
      log.info(
          "Deducted {} units from '{}'. Remaining: {}",
          orderedQuantity,
          product.getName(),
          newQuantity);
    }
  }

  private Address getShippingAddress(CheckoutRequest request, User user) {
    if (request.getShippingAddress() != null) {
      return addressRepository
          .findByIdAndUserId(request.getShippingAddress(), user.getId())
          .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }
    return addressRepository
        .findByUserIdAndIsDefaultTrue(user.getId())
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "No default address found. Please add a delivery address first."));
  }

  private void applyCouponToSummary(CheckoutSummary summary, String couponCode, Long userId) {
    if (StringUtils.isBlank(couponCode)) return;
    try {
      ApplyCouponRequest req = new ApplyCouponRequest();
      req.setCode(couponCode);
      req.setOrderTotal(summary.getFinalTotal());
      ApplyCouponResponse result = couponService.applyCoupon(req, userId);
      summary.setCouponCode(result.getCode());
      summary.setCouponDiscount(result.getDiscountAmount());
      summary.setAppliedCouponId(result.getCouponId());
      log.info("Applied coupon {} with discount: {}", couponCode, result.getDiscountAmount());
    } catch (BadRequestException e) {
      log.warn("Coupon not applied: {}", e.getMessage());
      summary.setCouponCode(null);
      summary.setCouponDiscount(BigDecimal.ZERO);
    }
  }

  private void recordCouponUsageIfApplicable(CheckoutSummary summary, Order order, User user) {
    if (summary.getAppliedCouponId() == null) return;
    couponService.redeemCoupon(
        summary.getAppliedCouponId(), user.getId(), order.getId(), summary.getCouponDiscount());
    log.info(
        "Recorded coupon usage for coupon ID: {}, order: {}",
        summary.getAppliedCouponId(),
        order.getOrderNumber());
  }

  private void sendOrderPlacedNotification(Order order, User user) {
    try {
      notificationService.createAndSendNotification(
          NotificationRequest.builder()
              .userId(user.getId())
              .title("Order Placed!")
              .message(
                  "Your order #"
                      + order.getOrderNumber()
                      + " has been received. Total: $"
                      + order.getTotalAmount())
              .type(NotificationType.ORDER_CREATED)
              .referenceId(String.valueOf(order.getId()))
              .referenceType("ORDER")
              .actionUrl("/orders/" + order.getId())
              .sendPush(true)
              .saveToDatabase(true)
              .expiresInDays(30)
              .build());
      log.info("Order placed notification sent for order: {}", order.getOrderNumber());
    } catch (Exception e) {
      log.warn("Failed to send order placed notification: {}", e.getMessage());
    }
  }

  private void sendOrderCancelledNotification(Order order, User user) {
    try {
      notificationService.createAndSendNotification(
          NotificationRequest.builder()
              .userId(user.getId())
              .title("Order Cancelled")
              .message("Your order #" + order.getOrderNumber() + " has been cancelled.")
              .type(NotificationType.ORDER_CANCELLED)
              .referenceId(String.valueOf(order.getId()))
              .referenceType("ORDER")
              .actionUrl("/orders/" + order.getId())
              .sendPush(true)
              .saveToDatabase(true)
              .expiresInDays(30)
              .build());
    } catch (Exception e) {
      log.warn("Failed to send cancel notification: {}", e.getMessage());
    }
  }

  @Transactional
  public void confirmCodOrderDelivery(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

    if (order.getPaymentMethod() != PaymentMethod.COD) {
      throw new BadRequestException("Only COD orders can be confirmed via this method");
    }

    if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
      throw new BadRequestException("Order is not in CONFIRMED status");
    }

    order.setOrderStatus(OrderStatus.DELIVERED);
    orderRepository.save(order);
    saveOrderStatusHistory(order, OrderStatus.DELIVERED);

    log.info("COD order #{} marked as delivered", order.getOrderNumber());
  }

  @Transactional
  public void confirmCashInShopPickup(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

    if (order.getPaymentMethod() != PaymentMethod.CASH_IN_SHOP) {
      throw new BadRequestException("Only Cash-in-Shop orders can be confirmed via this method");
    }

    if (order.getOrderStatus() != OrderStatus.READY_FOR_PICKUP) {
      throw new BadRequestException("Order is not in READY_FOR_PICKUP status");
    }

    // Check if payment has expired
    Payment payment =
        paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

    if (payment.getExpiredAt() != null && LocalDateTime.now().isAfter(payment.getExpiredAt())) {
      throw new BadRequestException("Payment reservation has expired. Order has been cancelled.");
    }

    order.setOrderStatus(OrderStatus.COMPLETED);
    orderRepository.save(order);
    saveOrderStatusHistory(order, OrderStatus.COMPLETED);

    log.info("Cash-in-Shop order #{} marked as picked up", order.getOrderNumber());
  }
}
