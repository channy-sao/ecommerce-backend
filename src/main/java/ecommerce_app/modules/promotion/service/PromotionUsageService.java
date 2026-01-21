// ecommerce_app/modules/promotion/service/PromotionUsageService.java
package ecommerce_app.modules.promotion.service;

import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import ecommerce_app.modules.user.model.entity.User;

public interface PromotionUsageService {
    void recordPromotionUsage(Promotion promotion, Order order, User user);
    boolean canUsePromotion(Promotion promotion, User user);
    Long getPromotionUsageCount(Long promotionId);
}