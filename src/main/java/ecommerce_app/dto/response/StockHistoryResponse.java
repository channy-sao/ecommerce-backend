package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.StockMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockHistoryResponse {
  private Long id;
  private String variantSku;
  private StockMovementType movementType;
  private Integer quantity;
  private Integer quantityBefore;
  private Integer quantityAfter;
  private String referenceType;
  private String referenceId;
  private String note;
  private LocalDateTime createdAt;
  private Long createdBy;
}
