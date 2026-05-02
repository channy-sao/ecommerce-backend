package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

  @NotNull(message = "Product ID is required")
  private Long productId;

  private Long variantId; // Optional - required only for variant products

  @NotNull(message = "Movement type is required")
  private StockMovementType movementType; // IN, OUT, ADJUSTMENT, RETURN

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Integer quantity;

  private Long referenceId;
  private String referenceType;
  private String note;
}
