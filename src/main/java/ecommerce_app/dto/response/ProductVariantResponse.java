package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
  private Integer lowStockThreshold;
  private String stockStatus;
  private Boolean isActive;
  private List<AttributeValueDto> attributeValues;
  @Schema(description = "Whether this variant is the default for the product", example = "true")
  private Boolean isDefault;

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
