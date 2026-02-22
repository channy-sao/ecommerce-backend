package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
  private Map<LocalDateTime, ProductImportResponse> productImports;
}
