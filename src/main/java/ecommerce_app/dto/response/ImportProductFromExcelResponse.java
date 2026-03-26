package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Result of importing products from an Excel file")
public class ImportProductFromExcelResponse {

  @Schema(description = "Number of products successfully imported", example = "45")
  private int successCount;

  @Schema(description = "Number of products that failed to import", example = "3")
  private int errorCount;

  @Schema(description = "Total number of products processed from the file", example = "48")
  private int totalCount;
}
