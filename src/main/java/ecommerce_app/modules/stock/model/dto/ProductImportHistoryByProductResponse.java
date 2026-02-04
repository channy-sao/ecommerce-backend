package ecommerce_app.modules.stock.model.dto;

import ecommerce_app.modules.product.model.dto.ProductResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
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
@Schema(description = "Product import history")
public class ProductImportHistoryByProductResponse {
  @Schema(description = "Representation of product")
  private ProductResponse product;

  @Schema(description = "Product import by date")
  private Map<Instant, ProductImportResponse> productImports;
}
