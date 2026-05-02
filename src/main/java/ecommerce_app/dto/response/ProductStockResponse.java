package ecommerce_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStockResponse {
  private Long productId;
  private String productName;
  private Boolean hasVariants;
  private Integer totalStock;
  private Integer lowStockCount;
  private Integer outOfStockCount;
  private List<VariantStockSummaryResponse> variants;
}
