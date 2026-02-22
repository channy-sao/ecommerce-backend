// ecommerce_app/modules/promotion/strategy/PromotionStrategy.java
package ecommerce_app.service.strategy;

import ecommerce_app.entity.Product;
import ecommerce_app.entity.Promotion;
import java.math.BigDecimal;

public interface PromotionStrategy {
    BigDecimal calculateDiscount(Promotion promotion, Product product, Integer quantity);

    default void validate(Promotion promotion) {
        // Default validation - can be overridden
    }
}