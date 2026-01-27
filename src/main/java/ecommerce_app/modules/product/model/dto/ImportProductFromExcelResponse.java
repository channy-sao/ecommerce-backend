package ecommerce_app.modules.product.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportProductFromExcelResponse {
  private int successCount;
  private int errorCount;
  private int totalCount;
}
