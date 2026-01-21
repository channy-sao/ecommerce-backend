// ecommerce_app/modules/promotion/model/dto/PromotionRequest.java
package ecommerce_app.modules.promotion.model.dto;

import ecommerce_app.constant.enums.PromotionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PromotionRequest {

  @Size(max = 50)
  private String code;

  @NotBlank
  @Size(min = 3, max = 100)
  private String name;

  @NotNull private PromotionType discountType;

  @DecimalMin(value = "0.0", inclusive = false)
  @DecimalMax(value = "100.0")
  private BigDecimal discountValue;

  @PositiveOrZero private Integer buyQuantity;

  @PositiveOrZero private Integer getQuantity;

  @NotNull private Boolean active = true;

  @Future private LocalDateTime startAt;

  @Future private LocalDateTime endAt;

  @Positive private Integer maxUsage;

  private List<Long> productIds;

  // Validation method
  public void validate() {
    if (discountType == PromotionType.PERCENTAGE
        && (discountValue == null || discountValue.compareTo(new BigDecimal("100")) > 0)) {
      throw new IllegalArgumentException("Percentage discount must be between 0 and 100");
    }

    if (discountType == PromotionType.BUY_X_GET_Y && (buyQuantity == null || getQuantity == null)) {
      throw new IllegalArgumentException("Buy X Get Y requires both buyQuantity and getQuantity");
    }

    if (endAt != null && startAt != null && endAt.isBefore(startAt)) {
      throw new IllegalArgumentException("End date must be after start date");
    }
  }
}
