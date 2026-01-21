package ecommerce_app.util;

import ecommerce_app.modules.cart.model.entity.Cart;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for calculating monetary amounts related to a shopping cart.
 *
 * <p>This class cannot be instantiated and should only be used statically.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AmountCalculatedUtils {

  /**
   * Calculates the total amount for all items in the provided cart. The total is computed as the
   * sum of (price × quantity) for each cart item.
   *
   * @param cart the {@link Cart} object containing items to calculate the total for
   * @return the total amount as {@link BigDecimal}
   * @throws NullPointerException if {@code cart.getCartItems()} is {@code null}
   */
  public static BigDecimal calculateTotalAmount(Cart cart) {
    return cart.getCartItems().stream()
        .map(cartItem -> cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static BigDecimal calculateTotalAmountWithPromotion(Cart cart, Promotion promotion) {
    BigDecimal subtotal = calculateTotalAmount(cart);

    if (promotion == null) {
      return subtotal;
    }

    // For actual discount calculation, you would use PromotionFacade
    // This is a simplified version
    BigDecimal discount = calculateDiscount(cart, promotion);

    return subtotal.subtract(discount);
  }

  private static BigDecimal calculateDiscount(Cart cart, Promotion promotion) {
    // This is a placeholder - actual discount calculation should use PromotionFacade
    // You might want to remove this method and always use PromotionFacade
    return BigDecimal.ZERO;
  }
}
