package ecommerce_app.modules.cart.service.impl;

import ecommerce_app.constant.enums.CartStatus;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.cart.model.entity.Cart;
import ecommerce_app.modules.cart.repository.CartItemRepository;
import ecommerce_app.modules.cart.repository.CartRepository;
import ecommerce_app.modules.cart.service.CartService;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartServiceImpl implements CartService {

  private static final Long USER_ID = 10L; // Replace it with actual user context or session
  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final CartItemRepository cartItemRepository;
  private final UserRepository userRepository;

  private User getCurrentUser() {
    //    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //    return userRepo.findByUsername(auth.getName())
    //            .orElseThrow(() -> new RuntimeException("User not found"));

    // fake user
    return userRepository
        .findById(USER_ID)
        .orElseThrow(() -> new ResourceNotFoundException("User", USER_ID));
  }

  @Override
  @Transactional
  public Cart addNewProductToCart(Long productId) {
    Cart cart = getOrCreateCart();
    Product prod =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    cart.addNewItem(prod);
    cart.setStatus(CartStatus.ACTIVE);
    return cartRepository.save(cart);
  }

  @Override
  @Transactional
  public Cart incrementItem(Long productId) {
    Cart cart = getOrCreateCart();
    Product prod =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    cart.incrementItem(prod);
    return cartRepository.save(cart);
  }

  @Override
  @Transactional
  public Cart decrementItem(Long productId) {
    Cart cart = getOrCreateCart();
    Product prod =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    cart.decrementItem(prod);
    return cartRepository.save(cart);
  }

  @Override
  public Cart getCart() {
    var current = getCurrentUser();

    // get active cart
    return cartRepository
        .findByUserIdAndStatus(current.getId(), CartStatus.ACTIVE)
        .orElseThrow(() -> new ResourceNotFoundException("Cart is not found"));
  }

  private Cart getOrCreateCart() {
    User user = getCurrentUser();

    // get or create a new cart
    return cartRepository
        .findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE)
        .orElseGet(
            () -> {
              Cart cart = new Cart();
              cart.setUser(user);
              cart.setStatus(CartStatus.ACTIVE);
              cart.setUuid(UUID.randomUUID());
              cart.setTotal(BigDecimal.ZERO);
              cart = cartRepository.save(cart);
              return cart;
            });
  }
}
