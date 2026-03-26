package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Product specification response")
public class ProductSpecResponse {

  @Schema(description = "Unique specification ID", example = "1")
  private Long id;

  @Schema(description = "Specification text", example = "Bluetooth 5.1")
  private String specText;

  @Schema(description = "Display order of the specification", example = "1")
  private Integer sortOrder;
}
