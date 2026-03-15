package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.CouponDiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CouponRequest", description = "Request object for coupon creation or update")
public class CouponRequest {

  @NotBlank(message = "Coupon code is required")
  @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
  @Pattern(
      regexp = "^[A-Z0-9_-]+$",
      message = "Coupon code must contain only uppercase letters, digits, hyphens, or underscores")
  @Schema(description = "Unique coupon code", example = "SAVE20")
  private String code;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  @Schema(description = "Description of the coupon", example = "Save 20% on your order")
  private String description;

  @NotNull(message = "Discount type is required")
  @Schema(description = "Type of discount (PERCENTAGE or FIXED_AMOUNT)")
  private CouponDiscountType discountType;

  @NotNull(message = "Discount value is required")
  @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
  @Schema(description = "Discount value", example = "20.00")
  private BigDecimal discountValue;

  @DecimalMin(value = "0.00", message = "Minimum order amount must be 0 or greater")
  @Builder.Default
  @Schema(
      description = "Minimum order amount required to apply coupon",
      example = "50.00",
      defaultValue = "0.00")
  private BigDecimal minOrderAmount = BigDecimal.ZERO;

  @DecimalMin(value = "0.01", message = "Max discount must be greater than 0")
  @Schema(description = "Maximum discount cap (optional)", example = "50.00")
  private BigDecimal maxDiscount;

  @Min(value = 1, message = "Usage limit must be at least 1")
  @Schema(description = "Maximum total usage limit (null = unlimited)", example = "100")
  private Integer usageLimit;

  @Min(value = 1, message = "Usage per user must be at least 1")
  @Builder.Default
  @Schema(description = "Maximum usage per user", example = "1", defaultValue = "1")
  private Integer usagePerUser = 1;

  @Builder.Default
  @Schema(description = "Whether the coupon is active", example = "true", defaultValue = "true")
  private Boolean isActive = true;

  @Future(message = "Start date must be in the future")
  @Schema(description = "Coupon start date and time", example = "2026-06-01T00:00:00")
  private LocalDateTime startDate;

  @Future(message = "End date must be in the future")
  @Schema(description = "Coupon end date and time", example = "2026-06-30T23:59:59")
  private LocalDateTime endDate;
}
