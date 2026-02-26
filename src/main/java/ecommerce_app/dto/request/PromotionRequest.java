package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.PromotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@Schema(name = "PromotionRequest", description = "Request payload for creating or updating a promotion")
public class PromotionRequest {

  @Schema(
          description = "Unique promotion code (optional for automatic promotions)",
          example = "SUMMER2026",
          maxLength = 50)
  @Size(max = 50)
  private String code;

  @Schema(
          description = "Promotion display name",
          example = "Summer Sale 2026",
          requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank
  @Size(min = 3, max = 100)
  private String name;

  @Schema(
          description = "Type of promotion (PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y)",
          requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  private PromotionType discountType;

  @Schema(
          description = "Discount value. For percentage type: 0–100. For fixed amount: monetary value.",
          example = "10.0")
  @DecimalMin(value = "0.0", inclusive = false)
  @DecimalMax(value = "100.0")
  private BigDecimal discountValue;

  @Schema(
          description = "Required quantity to buy (used for BUY_X_GET_Y)",
          example = "2")
  @PositiveOrZero
  private Integer buyQuantity;

  @Schema(
          description = "Quantity given for free (used for BUY_X_GET_Y)",
          example = "1")
  @PositiveOrZero
  private Integer getQuantity;

  @Schema(
          description = "Whether the promotion is currently active",
          example = "true",
          defaultValue = "true")
  @NotNull
  private Boolean active = true;

  @Schema(
          description = "Promotion start date and time",
          example = "2026-06-01T00:00:00")
  @Future
  private LocalDateTime startAt;

  @Schema(
          description = "Promotion end date and time",
          example = "2026-06-30T23:59:59")
  @Future
  private LocalDateTime endAt;

  @Schema(
          description = "Maximum number of times the promotion can be used",
          example = "100")
  @Positive
  private Integer maxUsage;

  @Schema(
          description = "List of product IDs that this promotion applies to (ignored if applyToAll = true)",
          example = "[1, 2, 3]")
  private List<Long> productIds;

  @Schema(
          description = "If true, promotion applies to all products",
          example = "false",
          defaultValue = "false")
  private boolean applyToAll;

  @Schema(
          description = "Minimum purchase amount required to apply promotion",
          example = "50.00",
          defaultValue = "0.00")
  @PositiveOrZero
  private BigDecimal minPurchaseAmount = BigDecimal.ZERO;

  // Validation method
  public void validate() {
    if (discountType == PromotionType.PERCENTAGE
            && (discountValue == null || discountValue.compareTo(new BigDecimal("100")) > 0)) {
      throw new IllegalArgumentException("Percentage discount must be between 0 and 100");
    }

    if (discountType == PromotionType.BUY_X_GET_Y
            && (buyQuantity == null || getQuantity == null)) {
      throw new IllegalArgumentException(
              "Buy X Get Y requires both buyQuantity and getQuantity");
    }

    if (endAt != null && startAt != null && endAt.isBefore(startAt)) {
      throw new IllegalArgumentException(
              "End date must be after start date");
    }
  }
}