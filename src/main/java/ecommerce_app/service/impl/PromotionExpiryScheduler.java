package ecommerce_app.service.impl;

import ecommerce_app.entity.Promotion;
import ecommerce_app.repository.CouponRepository;
import ecommerce_app.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionExpiryScheduler {

    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;
    private final PromotionNotificationService promotionNotificationService;

    /**
     * Check promotions expiring in 24 hours — runs every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    public void notifyExpiringPromotions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24Hours = now.plusHours(24);

        List<Promotion> expiring = promotionRepository
            .findByActiveTrueAndEndAtBetween(now, in24Hours);

        expiring.forEach(promotion -> {
            log.info("Notifying expiring promotion: {}", promotion.getName());
            promotionNotificationService.notifyPromotionExpiring(promotion);
        });
    }
}