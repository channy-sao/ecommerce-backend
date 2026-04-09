package ecommerce_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class POSProductResponse {
  private Long id;
  private String name;
  private String sku;
  private String barcode;
  private BigDecimal price;
  private Integer availableStock;
  private String categoryName;
  private String imageUrl;
}