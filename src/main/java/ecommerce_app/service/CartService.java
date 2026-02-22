package ecommerce_app.service;

import ecommerce_app.dto.response.CartResponse;

public interface CartService {
  CartResponse addNewProductToCart(Long productId, Long userId);

  CartResponse incrementItem(Long itemId, Long userId);

  CartResponse decrementItem(Long itemId, Long userId);

  CartResponse getCart(Long userId);
}
