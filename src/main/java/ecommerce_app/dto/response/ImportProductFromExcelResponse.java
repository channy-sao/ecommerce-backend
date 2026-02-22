package ecommerce_app.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportProductFromExcelResponse {
  private int successCount;
  private int errorCount;
  private int totalCount;
}
