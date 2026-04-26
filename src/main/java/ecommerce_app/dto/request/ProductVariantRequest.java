package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request payload for creating or updating a product variant")
public class ProductVariantRequest {

  @Schema(
      description =
          "Variant ID — null for creating a new variant, non-null for updating an existing variant",
      example = "1")
  private Long id;

  @Schema(
      description = "Stock Keeping Unit (SKU) - unique identifier for the variant",
      example = "TSHIRT-RED-M")
  @NotBlank
  private String sku;

  @Schema(
      description = "Price of the variant. If null, the product's base price will be used",
      example = "19.99")
  private BigDecimal price;

  @Schema(
      description = "Available stock quantity for this variant",
      example = "100",
      minimum = "0",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  @Min(0)
  private Integer stockQuantity;

  @Schema(description = "Threshold to trigger low stock alert", example = "10", minimum = "0")
  @Min(0)
  private Integer lowStockThreshold;

  @Schema(
      description =
          "List of attribute value IDs associated with this variant (e.g., colorId, sizeId)",
      example = "[1, 2]",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotEmpty
  private List<Long> attributeValueIds;
}
