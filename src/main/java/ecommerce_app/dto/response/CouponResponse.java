package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.CouponDiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Coupon response payload")
public class CouponResponse {

  @Schema(description = "Unique coupon ID", example = "1")
  private Long id;

  @Schema(description = "Coupon code", example = "SAVE10")
  private String code;

  @Schema(description = "Coupon description", example = "Get $10 off on orders above $50")
  private String description;

  @Schema(description = "Type of discount the coupon provides", example = "FIXED_AMOUNT")
  private CouponDiscountType discountType;

  @Schema(description = "Discount value", example = "10.00")
  private BigDecimal discountValue;

  @Schema(description = "Minimum order amount required to use this coupon", example = "50.00")
  private BigDecimal minOrderAmount;

  @Schema(description = "Maximum discount amount allowed", example = "20.00")
  private BigDecimal maxDiscount;

  @Schema(description = "Total number of times this coupon can be used", example = "100")
  private Integer usageLimit;

  @Schema(description = "Maximum number of times a single user can use this coupon", example = "1")
  private Integer usagePerUser;

  @Schema(description = "Total number of times this coupon has been used so far", example = "45")
  private Integer usedCount;

  @Schema(description = "Whether the coupon is currently active", example = "true")
  private Boolean isActive;

  @Schema(description = "Date and time the coupon becomes valid", example = "2024-06-01T00:00:00")
  private LocalDateTime startDate;

  @Schema(description = "Date and time the coupon expires", example = "2024-08-31T23:59:59")
  private LocalDateTime endDate;

  @Schema(
      description = "Current validity status of the coupon",
      example = "ACTIVE",
      allowableValues = {"SCHEDULED", "ACTIVE", "EXPIRED"})
  private String validityStatus;

  @Schema(description = "Date and time the coupon was created", example = "2024-01-01T10:00:00")
  private LocalDateTime createdAt;
}
