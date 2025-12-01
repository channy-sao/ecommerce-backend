package ecommerce_app.modules.cart.service;

import ecommerce_app.modules.cart.model.entity.Cart;

public interface CartService {
    Cart addNewProductToCart(Long productId);

    Cart incrementItem(Long productId);
    Cart decrementItem(Long productId);
    Cart getCart();
}
