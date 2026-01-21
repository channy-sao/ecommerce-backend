// ecommerce_app/modules/promotion/strategy/BuyXGetYStrategy.java
package ecommerce_app.modules.promotion.strategy;

import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component("buyxgety")
public class BuyXGetYStrategy implements PromotionStrategy {

  @Override
  public BigDecimal calculateDiscount(Promotion promotion, Product product, Integer quantity) {
    int buy = promotion.getBuyQuantity();
    int get = promotion.getGetQuantity();
    int batch = buy + get;

    if (batch == 0) return BigDecimal.ZERO;

    int fullBatches = quantity / batch;
    int freeItems = fullBatches * get;
    int remaining = quantity % batch;

    if (remaining > buy) {
      freeItems += (remaining - buy);
    }

    return product.getPrice().multiply(BigDecimal.valueOf(freeItems));
  }

  @Override
  public void validate(Promotion promotion) {
    if (promotion.getBuyQuantity() == null || promotion.getGetQuantity() == null) {
      throw new IllegalArgumentException("Buy X Get Y requires both buyQuantity and getQuantity");
    }

    if (promotion.getBuyQuantity() <= 0) {
      throw new IllegalArgumentException("buyQuantity must be greater than 0");
    }

    if (promotion.getGetQuantity() <= 0) {
      throw new IllegalArgumentException("getQuantity must be greater than 0");
    }
  }
}
