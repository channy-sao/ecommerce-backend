package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.constant.enums.StockStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response after a stock adjustment operation")
public class VariantStockResponse {

  @Schema(description = "Variant ID", example = "100")
  private Long variantId;

  @Schema(description = "Variant SKU", example = "PROD-RED-L")
  private String variantSku;

  @Schema(description = "Product ID", example = "50")
  private Long productId;

  @Schema(description = "Product name", example = "iPhone 15")
  private String productName;

  @Schema(description = "Quantity before adjustment", example = "50")
  private Integer quantityBefore;

  @Schema(description = "Quantity after adjustment", example = "100")
  private Integer quantityAfter;

  @Schema(description = "Movement type (IN, OUT, ADJUSTMENT, RETURN)", example = "IN")
  private StockMovementType movementType;

  @Schema(description = "Current stock status after adjustment", example = "IN_STOCK")
  private StockStatus stockStatus;

  @Schema(description = "Reference type (IMPORT, ORDER, MANUAL, etc.)", example = "IMPORT")
  private String referenceType;

  @Schema(description = "Timestamp when adjustment was made", example = "2024-07-01T10:00:00")
  private LocalDateTime adjustedAt;

  @Schema(description = "User who performed the adjustment")
  private AuditUserDto adjustedBy;
}
