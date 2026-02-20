package ecommerce_app.modules.product.model.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearEmptyStockResponse {
  private Long id;
  private String name;
  private BigDecimal price;
  private String primaryImage;
  private String categoryName;
  private int currentQuantity;
  private int threshold;
  private String stockStatus;
}
