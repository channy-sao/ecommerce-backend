package ecommerce_app.modules.promotion.strategy; // ecommerce_app/modules/promotion/strategy/PercentageDiscountStrategy.java

import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.promotion.model.entity.Promotion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component("percentage")
public class PercentageDiscountStrategy implements PromotionStrategy {

  @Override
  public BigDecimal calculateDiscount(Promotion promotion, Product product, Integer quantity) {
    BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    BigDecimal discountPercentage =
        promotion.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

    return totalPrice.multiply(discountPercentage).setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public void validate(Promotion promotion) {
    if (promotion.getDiscountValue() == null) {
      throw new IllegalArgumentException("Percentage discount requires discount value");
    }

    if (promotion.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0
        || promotion.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
      throw new IllegalArgumentException("Percentage must be between 0 and 100");
    }
  }
}
