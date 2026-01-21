// ecommerce_app/modules/promotion/strategy/PromotionStrategy.java
package ecommerce_app.modules.promotion.strategy;

import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.math.BigDecimal;

public interface PromotionStrategy {
    BigDecimal calculateDiscount(Promotion promotion, Product product, Integer quantity);

    default void validate(Promotion promotion) {
        // Default validation - can be overridden
    }
}