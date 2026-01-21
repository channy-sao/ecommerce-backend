// ecommerce_app/modules/promotion/strategy/FreeShippingStrategy.java
package ecommerce_app.modules.promotion.strategy;

import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component("freeshipping")
public class FreeShippingStrategy implements PromotionStrategy {

  @Override
  public BigDecimal calculateDiscount(Promotion promotion, Product product, Integer quantity) {
    // Free shipping doesn't give product discounts
    return BigDecimal.ZERO;
  }

  @Override
  public void validate(Promotion promotion) {
    // No specific validation needed
  }
}
