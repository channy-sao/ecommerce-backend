package ecommerce_app.modules.cart.service.impl;

import ecommerce_app.constant.enums.CartStatus;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.mapper.CartMapper;
import ecommerce_app.modules.cart.model.dto.CartResponse;
import ecommerce_app.modules.cart.model.entity.Cart;
import ecommerce_app.modules.cart.model.entity.CartItem;
import ecommerce_app.modules.cart.repository.CartItemRepository;
import ecommerce_app.modules.cart.repository.CartRepository;
import ecommerce_app.modules.cart.service.CartService;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
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
  private final UserRepository userRepository;
  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final CartItemRepository cartItemRepository;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public CartResponse addNewProductToCart(Long productId, Long userId) {
    Cart cart = getOrCreateCart(userId);
    Product prod =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    cart.addNewItem(prod);
    cart.setStatus(CartStatus.ACTIVE);
    Cart save = cartRepository.save(cart);
    return CartMapper.toCartResponse(save);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public CartResponse incrementItem(Long itemId, Long userId) {
    // 1. Get user's cart
    Cart cart = getOrCreateCart(userId);
    // 2. Find the specific cart item (by itemId)
    CartItem cartItem = getCartItemByItemIdAndCartId(itemId, cart.getId());
    // 3. Increment that specific item
    cartItem.increment();
    Cart save = cartRepository.save(cart);
    return CartMapper.toCartResponse(save);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public CartResponse decrementItem(Long itemId, Long userId) {
    // 1. Get user's cart
    Cart cart = getOrCreateCart(userId);
    // 2. Find the specific cart item (by itemId)
    CartItem cartItem = getCartItemByItemIdAndCartId(itemId, cart.getId());
    // 3. Decrement that specific item
    cartItem.decrement();
    Cart save = cartRepository.save(cart);
    return CartMapper.toCartResponse(save);
  }

  @Override
  public CartResponse getCart(Long userId) {
    // get active cart
    final var cart =
        cartRepository
            .findByUserIdAndStatus(userId, CartStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Cart is not found"));
    return CartMapper.toCartResponse(cart);
  }

  private Cart getOrCreateCart(Long userId) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User"));

    // get or create a new cart
    return cartRepository
        .findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE)
        .orElseGet(
            () -> {
              Cart cart = new Cart();
              cart.setUser(user);
              cart.setStatus(CartStatus.ACTIVE);
              cart.setUuid(UUID.randomUUID());
              cart = cartRepository.save(cart);
              return cart;
            });
  }

  private CartItem getCartItemByItemIdAndCartId(Long itemId, Long cartId) {
    return cartItemRepository
        .findByIdAndCartId(itemId, cartId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
  }
}
