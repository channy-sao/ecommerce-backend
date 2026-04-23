package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.NotificationType;
import ecommerce_app.dto.request.NotificationRequest;
import ecommerce_app.entity.Coupon;
import ecommerce_app.entity.Promotion;
import ecommerce_app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionNotificationService {

  private final NotificationService notificationService;

  // ✅ Removed unused userRepository

  /** Notify ALL users when new promotion is created. */
  public void notifyNewPromotion(Promotion promotion) {
    log.info("Sending new promotion notification: {}", promotion.getName());

    notificationService.sendBroadcastNotification(
        NotificationRequest.builder()
            .title("🎉 New Promotion!")
            .message(buildPromotionMessage(promotion))
            .type(NotificationType.NEW_PROMOTION)
            .referenceId(String.valueOf(promotion.getId()))
            .referenceType("PROMOTION")
            .sendPush(true)
            .saveToDatabase(true)
            .build());
  }

  /** Notify ALL users when new public coupon is created. */
  public void notifyNewCoupon(Coupon coupon) {
    log.info("Sending new coupon notification: {}", coupon.getCode());

    notificationService.sendBroadcastNotification(
        NotificationRequest.builder()
            .title("🎟️ New Coupon Available!")
            .message(buildCouponMessage(coupon))
            .type(NotificationType.NEW_COUPON)
            .referenceId(String.valueOf(coupon.getId()))
            .referenceType("COUPON")
            .sendPush(true)
            .saveToDatabase(true)
            .build());
  }

  /** Notify SPECIFIC user when coupon is assigned to them. */
  public void notifyCouponAssigned(Coupon coupon, Long userId) {
    log.info("Sending coupon assigned notification to user: {}", userId);

    notificationService.createAndSendNotification(
        NotificationRequest.builder()
            .userId(userId)
            .title("🎁 You Got a Special Coupon!")
            .message(buildCouponMessage(coupon))
            .type(NotificationType.COUPON_ASSIGNED)
            .referenceId(String.valueOf(coupon.getId()))
            .referenceType("COUPON")
            .sendPush(true)
            .saveToDatabase(true)
            // ✅ Only set if future date — prevent negative days
            .expiresInDays(
                coupon.getEndDate() != null && coupon.getEndDate().isAfter(LocalDateTime.now())
                    ? (int) LocalDate.now().until(coupon.getEndDate().toLocalDate()).getDays()
                    : null)
            .build());
  }

  /** Notify ALL users when promotion is expiring soon. */
  public void notifyPromotionExpiring(Promotion promotion) {
    log.info("Sending promotion expiring notification: {}", promotion.getName());

    notificationService.sendBroadcastNotification(
        NotificationRequest.builder()
            .title("⏰ Promotion Ending Soon!")
            .message("Don't miss out! " + promotion.getName() + " ends in 24 hours.")
            .type(NotificationType.PROMOTION_EXPIRING)
            .referenceId(String.valueOf(promotion.getId()))
            .referenceType("PROMOTION")
            .sendPush(true)
            .saveToDatabase(false)
            .build());
  }

  // ─── Message Builders ─────────────────────────────────────────────────────

  private String buildPromotionMessage(Promotion promotion) {
    return switch (promotion.getDiscountType()) {
      case PERCENTAGE ->
          promotion.getName()
              + " — Get "
              + promotion.getDiscountValue().intValue()
              + "% off! "
              + buildExpiry(promotion.getEndAt());
      case FIXED_AMOUNT ->
          promotion.getName()
              + " — Get $"
              + promotion.getDiscountValue()
              + " off! "
              + buildExpiry(promotion.getEndAt());
      case BUY_X_GET_Y ->
          promotion.getName()
              + " — Buy "
              + promotion.getBuyQuantity()
              + " Get "
              + promotion.getGetQuantity()
              + " Free! "
              + buildExpiry(promotion.getEndAt());
      case FREE_SHIPPING ->
          promotion.getName()
              + " — Free Shipping on your order! "
              + buildExpiry(promotion.getEndAt());
    };
  }

  private String buildCouponMessage(Coupon coupon) {
    String minOrder =
        coupon.getMinOrderAmount() != null ? " Min order $" + coupon.getMinOrderAmount() : "";

    return switch (coupon.getDiscountType()) {
      case PERCENTAGE ->
          "Use code "
              + coupon.getCode()
              + " for "
              + coupon.getDiscountValue().intValue()
              + "% off!"
              + minOrder;
      case FIXED_AMOUNT ->
          "Use code "
              + coupon.getCode()
              + " to get $"
              + coupon.getDiscountValue()
              + " off!"
              + minOrder;
      // ✅ Handle FREE_SHIPPING — was returning null before
      case FREE_SHIPPING -> "Use code " + coupon.getCode() + " for free shipping!" + minOrder;
    };
  }

  private String buildExpiry(LocalDateTime endAt) {
    if (endAt == null) return "";
    return "Ends " + endAt.toLocalDate();
  }
}
