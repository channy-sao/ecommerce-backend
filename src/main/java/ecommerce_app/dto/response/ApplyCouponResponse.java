// ApplyCouponResponse.java — what mobile gets back
package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.CouponDiscountType;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyCouponResponse {
  private Long couponId;
  private String code;
  private CouponDiscountType discountType;
  private BigDecimal discountValue;
  private BigDecimal discountAmount; // actual amount to deduct
  private BigDecimal finalTotal; // orderTotal - discountAmount
  private String message; // "Coupon applied! You save $10.00"
}
