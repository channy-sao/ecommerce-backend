package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.CartStatus;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.core.SimpleTry;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.OrderMapper;
import ecommerce_app.entity.Address;
import ecommerce_app.repository.AddressRepository;
import ecommerce_app.entity.Cart;
import ecommerce_app.entity.CartItem;
import ecommerce_app.repository.CartRepository;
import ecommerce_app.dto.request.CheckoutRequest;
import ecommerce_app.dto.CheckoutSummary;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.OrderItem;
import ecommerce_app.entity.OrderStatusHistory;
import ecommerce_app.repository.OrderItemRepository;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.repository.OrderStatusHistoryRepository;
import ecommerce_app.service.OrderService;
import ecommerce_app.entity.Product;
import ecommerce_app.service.facade.PromotionFacade;
import ecommerce_app.entity.Promotion;
import ecommerce_app.entity.PromotionUsage;
import ecommerce_app.repository.PromotionUsageRepository;
import ecommerce_app.entity.Stock;
import ecommerce_app.repository.StockRepository;
import ecommerce_app.entity.User;
import ecommerce_app.repository.UserRepository;
import ecommerce_app.util.JsonUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecommerce_app.util.SimpleShippingCalculator;
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
  private final StockRepository stockRepository;

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

    // deduct stock after order items saved
    deductStock(cart);
    log.info("Stock deducted for order: {}", savedOrder.getOrderNumber());

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

      // ✅ 1. Check promotion exists
      if (promotion == null) {
        summary.setPromotionError("Promotion code not found");
        log.warn("Promotion code not found: {}", promotionCode);
        return;
      }

      // ✅ 2. Check promotion is active
      if (!promotionFacade.isPromotionActive(promotion)) {
        summary.setPromotionError("Promotion is not active or expired");
        log.warn("Promotion is not active: {}", promotionCode);
        return;
      }

      // ✅ 3. Check max usage limit
      if (promotion.getMaxUsage() != null && promotion.getUsages() != null) {
        int currentUsage = promotion.getUsages().size();
        if (currentUsage >= promotion.getMaxUsage()) {
          summary.setPromotionError("Promotion usage limit reached");
          log.warn("Promotion usage limit reached for: {}", promotionCode);
          return;
        }
      }

      // ✅ 4. Check min purchase amount
      if (promotion.getMinPurchaseAmount() != null
          && summary.getSubtotal().compareTo(promotion.getMinPurchaseAmount()) < 0) {
        summary.setPromotionError(
            "Minimum purchase of $" + promotion.getMinPurchaseAmount() + " required");
        return;
      }

      // ✅ 5. FREE_SHIPPING — skip item loop, handled separately
      if (promotion.getDiscountType() == PromotionType.FREE_SHIPPING) {
        summary.setAppliedPromotion(promotion);
        summary.setTotalDiscount(BigDecimal.ZERO);
        log.info("Applied FREE_SHIPPING promotion: {}", promotionCode);
        return; // shipping discount handled in isFreeShippingPromotion()
      }

      // ✅ 6. Apply promotion to items
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
              BigDecimal maxItemDiscount =
                  product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

              // Cap at item price
              itemDiscount = itemDiscount.min(maxItemDiscount);

              // Cap at subtotal
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

      // ✅ 7. Cap total discount at subtotal
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
    if (summary.getAppliedPromotion() != null) {
      PromotionUsage usage =
          PromotionUsage.builder()
              .promotion(summary.getAppliedPromotion())
              .order(order)
              .user(user)
              .usedAt(LocalDateTime.now())
              .discountAmount(
                  summary.getTotalDiscount() != null ? summary.getTotalDiscount() : BigDecimal.ZERO)
              .build();
      promotionUsageRepository.save(usage);
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
          "Deducted {} units from product '{}'. Remaining stock: {}",
          orderedQuantity,
          product.getName(),
          newQuantity);
    }
  }

  private Address getShippingAddress(CheckoutRequest request, User user) {
    // User selected specific address
    if (request.getShippingAddress() != null) {
      return addressRepository
          .findByIdAndUserId(request.getShippingAddress(), user.getId())
          .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    // Fallback to default address
    return addressRepository
        .findByUserIdAndIsDefaultTrue(user.getId())
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "No default address found. Please add a delivery address first."));
  }
}
