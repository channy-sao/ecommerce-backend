package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.CouponDiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after applying a coupon to an order")
public class ApplyCouponResponse {

  @Schema(description = "Unique coupon ID", example = "5")
  private Long couponId;

  @Schema(description = "Coupon code that was applied", example = "SAVE10")
  private String code;

  @Schema(description = "Type of discount the coupon provides", example = "FIXED_AMOUNT")
  private CouponDiscountType discountType;

  @Schema(description = "Discount value defined on the coupon", example = "10.00")
  private BigDecimal discountValue;

  @Schema(description = "Actual discount amount deducted from the order total", example = "10.00")
  private BigDecimal discountAmount;

  @Schema(description = "Final order total after discount is applied", example = "89.99")
  private BigDecimal finalTotal;

  @Schema(
      description = "Human-readable message about the coupon result",
      example = "Coupon applied! You save $10.00")
  private String message;
}
