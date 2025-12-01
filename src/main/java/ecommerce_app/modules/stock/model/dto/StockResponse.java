package ecommerce_app.modules.stock.model.dto;

import ecommerce_app.modules.product.model.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
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
@Schema(description = "Stock response DTO containing stock and product information")
public class StockResponse {

  @Schema(description = "Stock ID", example = "1")
  private Long id;

  @Schema(description = "Associated product object")
  private Long productId;

  @Schema(description = "Quantity of the product in stock", example = "100")
  private int quantity;

  @Schema(description = "User ID who created the stock record", example = "10")
  private Long createdBy;

  @Schema(description = "User ID who last updated the stock record", example = "15")
  private Long updatedBy;

  @Schema(description = "Timestamp when the stock record was created", example = "2024-07-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "Timestamp when the stock record was last updated", example = "2024-07-15T15:30:00")
  private LocalDateTime updatedAt;
}
