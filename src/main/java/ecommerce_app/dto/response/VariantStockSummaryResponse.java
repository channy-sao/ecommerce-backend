package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.StockStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Variant stock summary for listings and queries")
public class VariantStockSummaryResponse {

  @Schema(description = "Variant ID", example = "100")
  private Long variantId;

  @Schema(description = "Variant SKU", example = "PROD-RED-L")
  private String sku;

  @Schema(description = "Current stock quantity", example = "75")
  private Integer stockQuantity;

  @Schema(description = "Low stock threshold for alerts", example = "10")
  private Integer lowStockThreshold;

  @Schema(description = "Current stock status", example = "IN_STOCK")
  private StockStatus stockStatus;

  @Schema(description = "Whether the variant is active", example = "true")
  private Boolean isActive;

  @Schema(
      description = "Attribute values (e.g., ['Red', 'Large'])",
      example = "[\"Red\", \"Large\"]")
  private List<String> attributeValues;

  @Schema(description = "Variant price (if different from product)", example = "999.99")
  private BigDecimal price;
}
