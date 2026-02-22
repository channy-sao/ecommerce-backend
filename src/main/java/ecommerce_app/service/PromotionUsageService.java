// ecommerce_app/modules/promotion/service/PromotionUsageService.java
package ecommerce_app.service;

import ecommerce_app.entity.Order;
import ecommerce_app.entity.Promotion;
import ecommerce_app.entity.User;

public interface PromotionUsageService {
    void recordPromotionUsage(Promotion promotion, Order order, User user);
    boolean canUsePromotion(Promotion promotion, User user);
    Long getPromotionUsageCount(Long promotionId);
}