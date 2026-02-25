// CouponResponse.java
package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.CouponDiscountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {
  private Long id;
  private String code;
  private String description;
  private CouponDiscountType discountType;
  private BigDecimal discountValue;
  private BigDecimal minOrderAmount;
  private BigDecimal maxDiscount;
  private Integer usageLimit;
  private Integer usagePerUser;
  private Integer usedCount;
  private Boolean isActive;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String validityStatus; // SCHEDULED / ACTIVE / EXPIRED
  private LocalDateTime createdAt;
}
