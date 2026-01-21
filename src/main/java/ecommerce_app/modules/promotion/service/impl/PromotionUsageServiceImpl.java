package ecommerce_app.modules.promotion.service.impl;

import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import ecommerce_app.modules.promotion.model.entity.PromotionUsage;
import ecommerce_app.modules.promotion.repository.PromotionUsageRepository;
import ecommerce_app.modules.promotion.service.PromotionUsageService;
import ecommerce_app.modules.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromotionUsageServiceImpl implements PromotionUsageService {

  private final PromotionUsageRepository promotionUsageRepository;

  @Override
  @Transactional
  public void recordPromotionUsage(Promotion promotion, Order order, User user) {
    PromotionUsage usage = new PromotionUsage();
    usage.setPromotion(promotion);
    usage.setOrder(order);
    usage.setUser(user);

    promotionUsageRepository.save(usage);
  }

  @Override
  public boolean canUsePromotion(Promotion promotion, User user) {
    if (promotion.getMaxUsage() == null) {
      return true;
    }

    Long currentUsage = promotionUsageRepository.countByPromotionId(promotion.getId());
    if (currentUsage >= promotion.getMaxUsage()) {
      return false;
    }

    // Check per-user limit if needed
    // Long userUsage = promotionUsageRepository.countByPromotionIdAndUserId(promotion.getId(),
    // user.getId());
    // return userUsage < promotion.getMaxUsagePerUser();

    return true;
  }

  @Override
  public Long getPromotionUsageCount(Long promotionId) {
    return promotionUsageRepository.countByPromotionId(promotionId);
  }
}
