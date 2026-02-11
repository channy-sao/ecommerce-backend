package ecommerce_app.modules.order.service.impl;

import ecommerce_app.constant.enums.CartStatus;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.core.SimpleTry;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.mapper.OrderMapper;
import ecommerce_app.modules.address.model.entity.Address;
import ecommerce_app.modules.address.repository.AddressRepository;
import ecommerce_app.modules.cart.model.entity.Cart;
import ecommerce_app.modules.cart.model.entity.CartItem;
import ecommerce_app.modules.cart.repository.CartRepository;
import ecommerce_app.modules.order.model.dto.CheckoutRequest;
import ecommerce_app.modules.order.model.dto.CheckoutSummary;
import ecommerce_app.modules.order.model.dto.OrderDetailResponse;
import ecommerce_app.modules.order.model.dto.OrderResponse;
import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.modules.order.model.entity.OrderStatusHistory;
import ecommerce_app.modules.order.repository.OrderItemRepository;
import ecommerce_app.modules.order.repository.OrderRepository;
import ecommerce_app.modules.order.repository.OrderStatusHistoryRepository;
import ecommerce_app.modules.order.service.OrderService;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.promotion.facade.PromotionFacade;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import ecommerce_app.modules.promotion.model.entity.PromotionUsage;
import ecommerce_app.modules.promotion.repository.PromotionUsageRepository;
import ecommerce_app.modules.shipping.service.SimpleShippingCalculator;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import ecommerce_app.util.JsonUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {
  private final OrderItemRepository orderItemRepository;
  private final PromotionUsageRepository promotionUsageRepository;
  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final AddressRepository addressRepository;
  private final PromotionFacade promotionFacade;
  private final SimpleShippingCalculator shippingCalculator;
  private final UserRepository userRepository;
  private final OrderStatusHistoryRepository orderStatusHistoryRepository;
  private final OrderMapper orderMapper;
  private final OrderNumberGenerator orderNumberGenerator;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public OrderResponse checkout(CheckoutRequest checkoutRequest, Long userId) {
    log.info("Checkout request received: {}", checkoutRequest);

    // Get user
    User currentUser = getUserById(userId);

    // Get shipping address
    Address shippingAddress = getShippingAddress(checkoutRequest, currentUser);

    // Retrieve active cart
    Cart cart = retrieveCart(currentUser.getId());

    // Validate cart
    validateCart(cart);

    // Calculate shipping cost
    BigDecimal shippingCost = calculateShippingCost(checkoutRequest, cart, shippingAddress);

    // Validate stock
    validateStockAvailability(cart);

    // Calculate checkout summary
    CheckoutSummary checkoutSummary =
        calculateCheckoutSummary(cart, checkoutRequest.getPromotionCode());

    // Apply free shipping if applicable
    if (isFreeShippingPromotion(checkoutSummary)) {
      shippingCost = BigDecimal.ZERO;
      checkoutSummary.setShippingDiscount(checkoutSummary.getShippingCost());
      checkoutSummary.setFreeShipping(true);
    }

    // Calculate totals
    final BigDecimal subtotalAmount = checkoutSummary.getSubtotal();
    final BigDecimal discountAmount = checkoutSummary.getTotalDiscount();
    final BigDecimal shippingAmount = shippingCost;
    final BigDecimal totalAmount = checkoutSummary.getFinalTotal().add(shippingAmount);

    log.info(
        "Checkout totals - Subtotal: {}, Discount: {}, Shipping: {}, Total: {}",
        subtotalAmount,
        discountAmount,
        shippingAmount,
        totalAmount);

    // Create order
    Order order =
        createOrder(
            currentUser,
            cart,
            checkoutRequest,
            shippingAddress,
            subtotalAmount,
            discountAmount,
            shippingAmount,
            totalAmount);

    String orderNumber = orderNumberGenerator.generateOrderNumber();
    order.setOrderNumber(orderNumber);

    log.debug("Generated order number: {}", orderNumber);

    // save order
    Order savedOrder = orderRepository.save(order);

    // save order history
    saveOrderStatusHistory(savedOrder, savedOrder.getOrderStatus());

    log.info("Order created with ID: {}", savedOrder.getId());

    // Create order items
    List<OrderItem> orderItems = createOrderItems(cart, savedOrder, checkoutSummary);
    orderItemRepository.saveAll(orderItems);
    log.info("Created {} order items", orderItems.size());

    // Record promotion usage if applicable
    recordPromotionUsageIfApplicable(checkoutSummary, savedOrder, currentUser);

    // Update cart status
    updateCartStatus(cart);

    return orderMapper.toCheckoutResponse(savedOrder);
  }

  @Transactional(readOnly = true)
  @Override
  public List<OrderResponse> getOrders(Long userId) {
    log.info("Fetching orders for user ID: {}", userId);
    return orderRepository.findByUserId(userId).stream()
        .map(orderMapper::toSimpleResponse)
        .toList();
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

  // ============ PRIVATE HELPER METHODS ============

  private User getUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
  }

  private Address getShippingAddress(CheckoutRequest checkoutRequest, User user) {
    return addressRepository
        .findByIdAndUserId(checkoutRequest.getShippingAddress(), user.getId())
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Address not found with ID: " + checkoutRequest.getShippingAddress()));
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

  private void validateStockAvailability(Cart cart) {
    for (CartItem cartItem : cart.getCartItems()) {
      Product product = cartItem.getProduct();
      int requestedQuantity = cartItem.getQuantity();

      // Assuming product has a stock quantity field
      int availableStock = product.getStockQuantity();

      if (requestedQuantity > availableStock) {
        throw new IllegalStateException(
            String.format(
                "Insufficient stock for product '%s'. Available: %d, Requested: %d",
                product.getName(), availableStock, requestedQuantity));
      }
    }
  }

  private CheckoutSummary calculateCheckoutSummary(Cart cart, String promotionCode) {
    CheckoutSummary summary = new CheckoutSummary();

    // Calculate subtotal
    BigDecimal subtotal = calculateSubtotal(cart);
    summary.setSubtotal(subtotal);

    // Initialize defaults
    summary.setTotalDiscount(BigDecimal.ZERO);
    summary.setShippingDiscount(BigDecimal.ZERO);
    summary.setFreeShipping(false);

    // Try to apply promotion
    if (StringUtils.isNotBlank(promotionCode)) {
      applyPromotionToSummary(summary, cart, promotionCode);
    } else {
      // Optionally, you can check for auto-apply promotions here
      applyAutoPromotionsToSummary(summary, cart);
    }

    // Calculate final total
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
        log.warn("Promotion code not found: {}", promotionCode);
        return;
      }

      // Validate promotion is active using facade
      if (!promotionFacade.isPromotionActive(promotion)) {
        summary.setPromotionError("Promotion is not active or expired");
        log.warn("Promotion is not active: {}", promotionCode);
        return;
      }

      // Check max usage limit
      if (promotion.getMaxUsage() != null && promotion.getUsages() != null) {
        int currentUsage = promotion.getUsages().size();
        if (currentUsage >= promotion.getMaxUsage()) {
          summary.setPromotionError("Promotion usage limit reached");
          log.warn("Promotion usage limit reached for: {}", promotionCode);
          return;
        }
      }
      // have promotion and apply to summary
      summary.setAppliedPromotion(promotion);

      // Calculate discounts
      BigDecimal totalDiscount = BigDecimal.ZERO;
      Map<Long, BigDecimal> itemDiscounts = new HashMap<>();

      for (CartItem cartItem : cart.getCartItems()) {
        Product product = cartItem.getProduct();

        if (promotionFacade.isProductEligibleForPromotion(product, promotion)) {
          try {
            BigDecimal itemDiscount =
                promotionFacade.calculateDiscount(promotion, product, cartItem.getQuantity());

            // Validate discount is not negative and not more than item price
            if (itemDiscount != null && itemDiscount.compareTo(BigDecimal.ZERO) > 0) {
              BigDecimal maxItemDiscount =
                  product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

              // Ensure discount doesn't exceed item price
              itemDiscount = itemDiscount.min(maxItemDiscount);

              // Ensure discount doesn't exceed subtotal
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

      // Ensure total discount doesn't exceed subtotal
      if (totalDiscount.compareTo(summary.getSubtotal()) > 0) {
        totalDiscount = summary.getSubtotal();
        log.warn("Adjusted total discount to match subtotal");
      }

      summary.setItemDiscounts(itemDiscounts);
      summary.setTotalDiscount(totalDiscount);
      summary.setPromotionError(null);

      log.info("Applied promotion {} with total discount: {}", promotionCode, totalDiscount);

    } catch (Exception e) {
      log.error("Error applying promotion {}: {}", promotionCode, e.getMessage(), e);
      summary.setPromotionError("Error applying promotion: " + e.getMessage());
    }
  }

  private void applyAutoPromotionsToSummary(CheckoutSummary summary, Cart cart) {
    // Calculate discounts
    try {
      BigDecimal totalDiscount = BigDecimal.ZERO;
      Map<Long, BigDecimal> itemDiscounts = new HashMap<>();
      for (CartItem cartItem : cart.getCartItems()) {
        final var product = cartItem.getProduct();
        var promotion =
            promotionFacade.getAvailablePromotion(product).stream().findFirst().orElse(null);
        if (promotion != null) {
          // have promotion and apply to summary
          summary.setAppliedPromotion(promotion);

          BigDecimal itemDiscount =
              promotionFacade.calculateDiscount(promotion, product, cartItem.getQuantity());

          if (itemDiscount != null && itemDiscount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal maxItemDiscount =
                product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            // Ensure discount doesn't exceed item price
            itemDiscount = itemDiscount.min(maxItemDiscount);

            // Ensure discount doesn't exceed subtotal
            itemDiscount = itemDiscount.min(summary.getSubtotal());

            itemDiscounts.put(product.getId(), itemDiscount);
            totalDiscount = totalDiscount.add(itemDiscount);
          }

          // Ensure total discount doesn't exceed subtotal
          if (totalDiscount.compareTo(summary.getSubtotal()) > 0) {
            totalDiscount = summary.getSubtotal();
            log.warn("Adjusted total discount to match subtotal");
          }

          summary.setItemDiscounts(itemDiscounts);
          summary.setTotalDiscount(totalDiscount);
          summary.setPromotionError(null);
          log.info(
              "Applied promotion {} with total discount: {}", promotion.getName(), totalDiscount);
        }
      }
    } catch (Exception e) {
      log.error("Error applying : {}", e.getMessage(), e);
      summary.setPromotionError("Error applying promotion: " + e.getMessage());
    }
  }

  private BigDecimal calculateFinalTotal(CheckoutSummary summary) {
    BigDecimal subtotal = summary.getSubtotal() != null ? summary.getSubtotal() : BigDecimal.ZERO;
    BigDecimal discount =
        summary.getTotalDiscount() != null ? summary.getTotalDiscount() : BigDecimal.ZERO;

    // Ensure discount doesn't exceed subtotal
    discount = discount.min(subtotal);

    return subtotal.subtract(discount);
  }

  private boolean isFreeShippingPromotion(CheckoutSummary checkoutSummary) {
    return checkoutSummary.getAppliedPromotion() != null
        && checkoutSummary.getAppliedPromotion().getDiscountType() == PromotionType.FREE_SHIPPING;
  }

  private void saveOrderStatusHistory(Order order, OrderStatus status) {
    OrderStatusHistory history =
        OrderStatusHistory.builder()
            .order(order)
            .status(status) // 🚨 THIS IS REQUIRED
            .build();

    orderStatusHistoryRepository.save(history);
  }

  private Order createOrder(
      User user,
      Cart cart,
      CheckoutRequest checkoutRequest,
      Address shippingAddress,
      BigDecimal subtotalAmount,
      BigDecimal discountAmount,
      BigDecimal shippingCost,
      BigDecimal totalAmount) {
    return Order.builder()
        .user(user)
        .cart(cart)
        .subtotalAmount(subtotalAmount)
        .discountAmount(discountAmount)
        .promotionCode(checkoutRequest.getPromotionCode())
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

      // Get discount for this item
      BigDecimal discountAmount = getItemDiscount(summary, product.getId(), quantity, unitPrice);

      // Calculate totals
      BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
      BigDecimal totalPrice = subtotal.subtract(discountAmount);

      // Ensure total price is not negative
      totalPrice = totalPrice.max(BigDecimal.ZERO);

      // Build order item
      OrderItem orderItem =
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
              .build();

      orderItems.add(orderItem);
    }

    return orderItems;
  }

  private BigDecimal getItemDiscount(
      CheckoutSummary summary, Long productId, int quantity, BigDecimal unitPrice) {
    BigDecimal discountAmount = BigDecimal.ZERO;

    if (summary.getItemDiscounts() != null) {
      discountAmount = summary.getItemDiscounts().getOrDefault(productId, BigDecimal.ZERO);
    }

    // Validate discount doesn't exceed item price
    BigDecimal maxPossibleDiscount = unitPrice.multiply(BigDecimal.valueOf(quantity));
    return discountAmount.min(maxPossibleDiscount);
  }

  private void recordPromotionUsageIfApplicable(CheckoutSummary summary, Order order, User user) {
    if (summary.getAppliedPromotion() != null
        && summary.getTotalDiscount() != null
        && summary.getTotalDiscount().compareTo(BigDecimal.ZERO) > 0) {

      try {
        PromotionUsage usage =
            PromotionUsage.builder()
                .promotion(summary.getAppliedPromotion())
                .order(order)
                .user(user)
                .usedAt(LocalDateTime.now())
                .discountAmount(summary.getTotalDiscount())
                .build();

        promotionUsageRepository.save(usage);
        log.info(
            "Recorded promotion usage for promotion: {} on order: {}",
            summary.getAppliedPromotion().getCode(),
            order.getId());
      } catch (Exception e) {
        log.error("Failed to record promotion usage: {}", e.getMessage(), e);
      }
    }
  }

  private void updateCartStatus(Cart cart) {
    cart.setStatus(CartStatus.CHECKED_OUT);
    cartRepository.save(cart);
    log.info("Updated cart {} status to CHECKED_OUT", cart.getId());
  }
}
