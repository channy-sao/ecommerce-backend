package ecommerce_app.infrastructure.mapper;

import ecommerce_app.modules.cart.model.dto.CartItemResponse;
import ecommerce_app.modules.cart.model.dto.CartResponse;
import ecommerce_app.modules.cart.model.entity.Cart;
import ecommerce_app.modules.cart.model.entity.CartItem;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import ecommerce_app.util.ProductMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CartMapper {
  public static CartResponse toCartResponse(Cart cart) {
    if (cart == null) return null;

    List<CartItemResponse> items = mapItems(cart.getCartItems());

    return CartResponse.builder()
        .id(cart.getId())
        .uuid(cart.getUuid())
        .total(calculateTotal(items))
        .totalItems(calculateItemCount(items))
        .subtotal(calculateTotal(items)) // Same as total for now
        .discount(BigDecimal.ZERO) // Add later if needed
        .status(cart.getStatus())
        .createdAt(cart.getCreatedAt())
        .updatedAt(cart.getUpdatedAt())
        .items(items)
        .userId(cart.getUser() != null ? cart.getUser().getId() : null)
        .userName(cart.getUser() != null ? cart.getUser().getFullName() : null)
        .userEmail(cart.getUser() != null ? cart.getUser().getEmail() : null)
        .build();
  }

  private static List<CartItemResponse> mapItems(List<CartItem> cartItems) {
    if (cartItems == null) return List.of();

    return cartItems.stream().map(CartMapper::toCartItemResponse).collect(Collectors.toList());
  }

  private static CartItemResponse toCartItemResponse(CartItem cartItem) {
    ProductResponse product = ProductMapper.toProductResponse(cartItem.getProduct());

    return CartItemResponse.builder()
        .id(cartItem.getId())
        .product(product)
        .quantity(cartItem.getQuantity())
        .itemTotal(calculateItemTotal(product, cartItem.getQuantity()))
        .build();
  }

  private static BigDecimal calculateItemTotal(ProductResponse product, Integer quantity) {
    return product.getPrice().multiply(BigDecimal.valueOf(quantity));
  }

  private static Integer calculateItemCount(List<CartItemResponse> items) {
    return items.stream().mapToInt(CartItemResponse::getQuantity).sum();
  }

  private static BigDecimal calculateTotal(List<CartItemResponse> items) {
    return items.stream()
        .map(CartItemResponse::getItemTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
