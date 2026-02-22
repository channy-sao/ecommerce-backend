package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Stock Update request")
public class UpdateStockRequest {
  @Schema(description = "stock quantity for adjust", example = "10")
  @NotNull(message = "Quantity is required")
  private int quantity;
}
