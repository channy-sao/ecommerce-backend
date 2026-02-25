// ecommerce_app/modules/promotion/strategy/FixedAmountStrategy.java
package ecommerce_app.service.strategy;

import ecommerce_app.entity.Product;
import ecommerce_app.entity.Promotion;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component("fixed")
public class FixedAmountStrategy implements PromotionStrategy {

  @Override
  public BigDecimal calculateDiscount(Promotion promotion, Product product, Integer quantity) {
    BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));

    // fixed discount on total, not per item
    // price=$100, qty=2, fixed=$30
    return promotion.getDiscountValue().min(totalPrice);
  }

  @Override
  public void validate(Promotion promotion) {
    if (promotion.getDiscountValue() == null) {
      throw new IllegalArgumentException("Fixed amount discount requires discount value");
    }

    if (promotion.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Fixed amount must be greater than 0");
    }
  }
}
