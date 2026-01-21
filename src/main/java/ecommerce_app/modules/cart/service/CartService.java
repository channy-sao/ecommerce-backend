package ecommerce_app.modules.cart.service;

import ecommerce_app.modules.cart.model.dto.CartResponse;

public interface CartService {
  CartResponse addNewProductToCart(Long productId, Long userId);

  CartResponse incrementItem(Long itemId, Long userId);

  CartResponse decrementItem(Long itemId, Long userId);

  CartResponse getCart(Long userId);
}
