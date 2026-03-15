package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.exception.BadRequestException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PromotionRequest", description = "Request payload for creating or updating a promotion")
public class PromotionRequest {

  @Size(max = 50, message = "Promotion code must not exceed 50 characters")
  @Pattern(
          regexp = "^[A-Z0-9_-]*$",
          message = "Promotion code must contain only uppercase letters, digits, hyphens, or underscores"
  )
  @Schema(description = "Unique promotion code (optional for automatic promotions)", example = "SUMMER2026")
  private String code;

  @NotBlank(message = "Promotion name is required")
  @Size(min = 3, max = 100, message = "Promotion name must be between 3 and 100 characters")
  @Schema(description = "Promotion display name", example = "Summer Sale 2026", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @NotNull(message = "Discount type is required")
  @Schema(description = "Type of promotion (PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y)", requiredMode = Schema.RequiredMode.REQUIRED)
  private PromotionType discountType;

  @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
  @DecimalMax(value = "100.0", message = "Discount value must not exceed 100")
  @Schema(description = "Discount value. For percentage type: 0–100. For fixed amount: monetary value.", example = "10.0")
  private BigDecimal discountValue;

  @PositiveOrZero(message = "Buy quantity must be 0 or greater")
  @Schema(description = "Required quantity to buy (used for BUY_X_GET_Y)", example = "2")
  private Integer buyQuantity;

  @PositiveOrZero(message = "Get quantity must be 0 or greater")
  @Schema(description = "Quantity given for free (used for BUY_X_GET_Y)", example = "1")
  private Integer getQuantity;

  @NotNull(message = "Active status is required")
  @Builder.Default
  @Schema(description = "Whether the promotion is currently active", example = "true", defaultValue = "true")
  private Boolean active = true;

  @Future(message = "Start date must be in the future")
  @Schema(description = "Promotion start date and time", example = "2026-06-01T00:00:00")
  private LocalDateTime startAt;

  @Future(message = "End date must be in the future")
  @Schema(description = "Promotion end date and time", example = "2026-06-30T23:59:59")
  private LocalDateTime endAt;

  @Positive(message = "Max usage must be greater than 0")
  @Schema(description = "Maximum number of times the promotion can be used", example = "100")
  private Integer maxUsage;

  @Schema(description = "List of product IDs that this promotion applies to (ignored if applyToAll = true)", example = "[1, 2, 3]")
  private List<Long> productIds;

  @Builder.Default
  @Schema(description = "If true, promotion applies to all products", example = "false", defaultValue = "false")
  private boolean applyToAll = false;

  @PositiveOrZero(message = "Minimum purchase amount must be 0 or greater")
  @Builder.Default
  @Schema(description = "Minimum purchase amount required to apply promotion", example = "50.00", defaultValue = "0.00")
  private BigDecimal minPurchaseAmount = BigDecimal.ZERO;
}