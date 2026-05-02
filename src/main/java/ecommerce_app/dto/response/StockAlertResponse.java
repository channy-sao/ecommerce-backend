package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.StockStatus;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StockAlertResponse {
  private Long variantId;
  private String variantSku;
  private Long productId;
  private Boolean inStock;
  private StockStatus stockStatus;
  private String productName;
  private Integer currentStock;
  private Integer lowStockThreshold;
  private List<String> attributeValues;
}
