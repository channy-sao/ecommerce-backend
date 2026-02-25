package ecommerce_app.util;

import ecommerce_app.entity.Promotion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PromotionCalculator {

  /** Calculate final discounted price after applying best promotion. */
  public static BigDecimal calculateDiscountedPrice(BigDecimal price, List<Promotion> promotions) {
    if (price == null) return BigDecimal.ZERO;
    if (promotions == null || promotions.isEmpty()) return price;

    return findBestPromotion(price, promotions)
        .map(promo -> applyDiscount(price, promo))
        .orElse(price);
  }

  /** Calculate discount percentage based on actual price difference. */
  public static Integer calculateDiscountPercentage(BigDecimal price, BigDecimal discountedPrice) {
    if (price == null || discountedPrice == null) return null;
    if (price.compareTo(BigDecimal.ZERO) == 0) return null;
    if (discountedPrice.compareTo(price) >= 0) return null;

    return price
        .subtract(discountedPrice)
        .divide(price, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .intValue();
  }

  /** Build human-readable promotion badge. */
  public static String buildPromotionBadge(
      BigDecimal price, List<Promotion> promotions, Integer discountPercentage) {

    if (promotions == null || promotions.isEmpty()) return null;

    return findBestPromotion(price, promotions)
        .map(
            promo ->
                switch (promo.getDiscountType()) {
                  case PERCENTAGE -> discountPercentage + "% OFF";
                  case FIXED_AMOUNT ->
                      "$" + promo.getDiscountValue().stripTrailingZeros().toPlainString() + " OFF";
                  case BUY_X_GET_Y ->
                      "BUY " + promo.getBuyQuantity() + " GET " + promo.getGetQuantity();
                  case FREE_SHIPPING -> "FREE SHIPPING";
                })
        .orElse(null);
  }

  /** Check if product has any active valid promotion. */
  public static Boolean hasPromotion(List<Promotion> promotions) {
    if (promotions == null || promotions.isEmpty()) return false;
    return promotions.stream().anyMatch(p -> p.getActive() && p.isCurrentlyValid());
  }

  // ─── Private Helpers ──────────────────────────────────────────────────────

  private static Optional<Promotion> findBestPromotion(
      BigDecimal price, List<Promotion> promotions) {
    return promotions.stream()
        .filter(Promotion::getActive)
        .filter(Promotion::isCurrentlyValid)
        .max(Comparator.comparing(p -> calculateSaving(price, p)));
  }

  private static BigDecimal applyDiscount(BigDecimal price, Promotion promo) {
    return switch (promo.getDiscountType()) {
      case PERCENTAGE -> {
        BigDecimal discount =
            price
                .multiply(promo.getDiscountValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        yield price.subtract(discount);
      }
      case FIXED_AMOUNT -> {
        BigDecimal result = price.subtract(promo.getDiscountValue());
        yield result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
      }
      // These don't affect price
      case BUY_X_GET_Y, FREE_SHIPPING -> price;
    };
  }

  private static BigDecimal calculateSaving(BigDecimal price, Promotion promo) {
    if (promo.getDiscountValue() == null) return BigDecimal.ZERO;
    return switch (promo.getDiscountType()) {
      case PERCENTAGE ->
          price
              .multiply(promo.getDiscountValue())
              .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
      case FIXED_AMOUNT -> promo.getDiscountValue().min(price);
      case BUY_X_GET_Y, FREE_SHIPPING -> BigDecimal.ZERO;
    };
  }
}
