package ecommerce_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
  private Long id;
  private String sku;
  private BigDecimal price;
  private BigDecimal effectivePrice;
  private Integer stockQuantity;
  private Integer lowStockThreshold; // ✅ add this
  private String stockStatus;
  private Boolean isActive;
  private List<AttributeValueDto> attributeValues;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AttributeValueDto {
    private Long id;
    private String attribute;
    private String value;
  }
}
