package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Mobile promotion validation response")
public class MobilePromotionValidationResponse {

  @Schema(description = "Whether the promotion code is valid", example = "true")
  private Boolean valid;

  @Schema(
      description = "Human-readable validation message",
      example = "Promotion applied! You save $10.00")
  private String message;

  @Schema(description = "Details of the validated promotion")
  private PromotionDetails promotion;

  @Schema(description = "Discount amount to be deducted from the order", example = "10.00")
  private BigDecimal discountAmount;

  @Schema(description = "Final order amount after discount", example = "89.99")
  private BigDecimal finalAmount;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Promotion details used in validation")
  public static class PromotionDetails {

    @Schema(description = "Unique promotion ID", example = "1")
    private Long id;

    @Schema(description = "Promotion code", example = "SUMMER25")
    private String code;

    @Schema(description = "Promotion name", example = "Summer Sale 2024")
    private String name;

    @Schema(description = "Type of discount", example = "PERCENTAGE")
    private String discountType;

    @Schema(description = "Discount value", example = "25.00")
    private BigDecimal discountValue;

    @Schema(description = "Buy quantity required for BUY_X_GET_Y promotions", example = "2")
    private Integer buyQuantity;

    @Schema(description = "Get quantity for BUY_X_GET_Y promotions", example = "1")
    private Integer getQuantity;

    @Schema(
        description = "Minimum purchase amount required to apply the promotion",
        example = "50.00")
    private BigDecimal minPurchaseAmount;
  }

  public static MobilePromotionValidationResponse invalid(String message) {
    return MobilePromotionValidationResponse.builder().valid(false).message(message).build();
  }

  public static MobilePromotionValidationResponse valid(
      String message,
      PromotionDetails promotion,
      BigDecimal discountAmount,
      BigDecimal finalAmount) {
    return MobilePromotionValidationResponse.builder()
        .valid(true)
        .message(message)
        .promotion(promotion)
        .discountAmount(discountAmount)
        .finalAmount(finalAmount)
        .build();
  }
}
