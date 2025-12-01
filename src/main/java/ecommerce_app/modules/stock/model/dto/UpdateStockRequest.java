package ecommerce_app.modules.stock.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Stock Update request")
public class UpdateStockRequest {
  @Schema(description = "stock quantity for adjust", example = "10")
  @Positive(message = "Quantity must be positive")
  @NotNull(message = "Quantity is required")
  private int quantity;
}
