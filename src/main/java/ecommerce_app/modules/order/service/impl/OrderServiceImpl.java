package ecommerce_app.modules.order.service.impl;

import ecommerce_app.constant.enums.CartStatus;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.cart.model.entity.Cart;
import ecommerce_app.modules.cart.model.entity.CartItem;
import ecommerce_app.modules.cart.repository.CartRepository;
import ecommerce_app.modules.cart.service.CartService;
import ecommerce_app.modules.order.model.dto.CheckoutRequest;
import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.modules.order.repository.OrderItemRepository;
import ecommerce_app.modules.order.repository.OrderRepository;
import ecommerce_app.modules.order.service.OrderService;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import ecommerce_app.util.AmountCalculatedUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {
  private final OrderItemRepository orderItemRepository;
  private final CartService cartService;
  private final OrderRepository orderRepository;
  private final ModelMapper modelMapper;
  private final UserRepository userRepository;
  private final CartRepository cartRepository;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public Order checkout(CheckoutRequest checkoutRequest) {
    // fixed user
    log.info("checkout request: {}", checkoutRequest);

    User currentUser =
        userRepository.findById(10L).orElseThrow(() -> new ResourceNotFoundException("User", 10L));

    log.info("checkout user: {}", currentUser);

    // Retrieve active cart of user
    Cart cart =
        cartRepository
            .findByUserIdAndStatus(currentUser.getId(), CartStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Cart is not found"));

    if (CollectionUtils.isEmpty(cart.getCartItems())) {
      throw new IllegalStateException("Cannot checkout an empty cart.");
    }

    // check enough quantity in the stock
    cart.getCartItems()
        .forEach(
            cartItem -> {
              var itemQuantity = cartItem.getQuantity();
              var stockQuantity =
                  cartItem.getProduct().getStocks().stream()
                      .findFirst()
                      .orElseThrow(
                          () -> new ResourceNotFoundException("Stock quantity is not found"))
                      .getQuantity();
              if (itemQuantity < stockQuantity) {
                throw new IllegalStateException("Cannot checkout an insufficient stock.");
              }
            });

    log.info("checkout cart: {}", cart.getId());

    // calculate total amount of in the cart
    final var totalAmount = AmountCalculatedUtils.calculateTotalAmount(cart);
    log.info("total amount: {}", totalAmount);

    // Create Order and OrderItems
    final var order =
        Order.builder()
            .user(currentUser)
            .cart(cart)
            .totalAmount(totalAmount)
            .orderStatus(OrderStatus.PENDING)
            .paymentStatus(PaymentStatus.PENDING)
            .paymentMethod(checkoutRequest.getPaymentMethod())
            .build();

    Order savedOrder = orderRepository.save(order);
    log.info("saved order: {}", order.getId());

    // Prepare Order Items
    // Convert cart items to order items
    List<OrderItem> orderItems = new ArrayList<>();
    for (CartItem cartItem : cart.getCartItems()) {
      OrderItem orderItem =
          OrderItem.builder()
              .product(cartItem.getProduct())
              .quantity(cartItem.getQuantity())
              .cart(cart)
              .subtotal(cartItem.getPrice())
              .order(savedOrder)
              .build();
      orderItems.add(orderItem);
    }
    orderItemRepository.saveAll(orderItems);
    log.info("save all order items: {}", orderItems);

    // Mark cart Checked_Out and saved change
    cart.setStatus(CartStatus.CHECKED_OUT);
    cart.setTotal(totalAmount);
    cartRepository.save(cart);
    log.info("Update status and saved cart: {}", cart.getId());

    return savedOrder;
  }

  @Transactional(readOnly = true)
  @Override
  public List<Order> getOrders() {
    // fixed user id
    log.info("get orders by user");
    return orderRepository.findByUserId(10L);
  }
}
