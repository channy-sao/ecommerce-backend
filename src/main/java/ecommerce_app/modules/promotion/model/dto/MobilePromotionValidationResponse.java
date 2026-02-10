package ecommerce_app.modules.promotion.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class MobilePromotionValidationResponse {

  private Boolean valid;
  private String message;
  private PromotionDetails promotion;
  private BigDecimal discountAmount;
  private BigDecimal finalAmount;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class PromotionDetails {
    private Long id;
    private String code;
    private String name;
    private String discountType;
    private BigDecimal discountValue;
    private Integer buyQuantity;
    private Integer getQuantity;
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
