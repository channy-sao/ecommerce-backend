// ecommerce_app/modules/promotion/strategy/FixedAmountStrategy.java
package ecommerce_app.modules.promotion.strategy;

import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component("fixed")
public class FixedAmountStrategy implements PromotionStrategy {

  @Override
  public BigDecimal calculateDiscount(Promotion promotion, Product product, Integer quantity) {
    BigDecimal discountPerItem = promotion.getDiscountValue();
    BigDecimal totalDiscount = discountPerItem.multiply(BigDecimal.valueOf(quantity));
    BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));

    // Ensure discount doesn't exceed total price
    return totalDiscount.min(totalPrice);
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
