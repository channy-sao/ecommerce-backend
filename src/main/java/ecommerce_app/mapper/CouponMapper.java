// CouponMapper.java
package ecommerce_app.mapper;

import ecommerce_app.dto.response.CouponResponse;
import ecommerce_app.entity.Coupon;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CouponMapper {

  public CouponResponse toResponse(Coupon coupon) {
    if (coupon == null) return null;
    return CouponResponse.builder()
        .id(coupon.getId())
        .code(coupon.getCode())
        .description(coupon.getDescription())
        .discountType(coupon.getDiscountType())
        .discountValue(coupon.getDiscountValue())
        .minOrderAmount(coupon.getMinOrderAmount())
        .maxDiscount(coupon.getMaxDiscount())
        .usageLimit(coupon.getUsageLimit())
        .usagePerUser(coupon.getUsagePerUser())
        .usedCount(coupon.getUsedCount())
        .isActive(coupon.getIsActive())
        .startDate(coupon.getStartDate())
        .endDate(coupon.getEndDate())
        .validityStatus(resolveValidityStatus(coupon))
        .createdAt(coupon.getCreatedAt())
        .build();
  }

  private String resolveValidityStatus(Coupon coupon) {
    LocalDateTime now = LocalDateTime.now();
    if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) return "SCHEDULED";
    if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) return "EXPIRED";
    return "ACTIVE";
  }
}
