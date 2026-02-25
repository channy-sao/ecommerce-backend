// ecommerce_app/modules/promotion/strategy/BuyXGetYStrategy.java
package ecommerce_app.service.strategy;

import ecommerce_app.entity.Product;
import ecommerce_app.entity.Promotion;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component("buyxgety")
public class BuyXGetYStrategy implements PromotionStrategy {

  /***
   * <p>
   * Buy 2, Get 1 Free, price=$100:
   * <p>
   * qty=1 → batches=0 → free=0 → discount=$0   ✅ not enough to trigger
   * qty=2 → batches=1 → free=1 → discount=$100 ✅
   * qty=3 → batches=1 → free=1 → discount=$100 ✅
   * qty=4 → batches=2 → free=2 → discount=$200 ✅
   * qty=6 → batches=3 → free=3 → discount=$300 ✅
   */
  @Override
  public BigDecimal calculateDiscount(Promotion promotion, Product product, Integer quantity) {
    int buy = promotion.getBuyQuantity();
    int get = promotion.getGetQuantity();

    if (buy <= 0) return BigDecimal.ZERO;

    // Batch = buy only — for every X bought, get Y free
    int fullBatches = quantity / buy;
    int freeItems = fullBatches * get;

    // Cap free items — can't be more than quantity paid for
    // e.g. Buy 1, Get 10 with qty=1 → only 1 free, not 10
    int paidItems = quantity - freeItems;
    if (freeItems > paidItems) {
      freeItems = paidItems;
    }

    return product
        .getPrice()
        .multiply(BigDecimal.valueOf(freeItems))
        .setScale(2, RoundingMode.HALF_UP);
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
