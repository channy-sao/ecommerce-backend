package ecommerce_app.modules.promotion.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobilePromotionValidationRequest {
  private String code;
  private Long userId;
  private BigDecimal cartTotal;
}
