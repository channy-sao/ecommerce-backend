package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Mobile product list response")
public class MobileProductListResponse {

  @Schema(description = "Internal product ID", example = "1")
  private Long id;

  @Schema(
      description = "Public unique identifier",
      example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID uuid;

  @Schema(description = "Product name", example = "iPhone 15 Pro")
  private String name;

  @Schema(
      description = "Short description of product",
      example = "Latest Apple smartphone with A17 chip")
  private String shortDescription;

  @Schema(description = "Original price", example = "999.99")
  private BigDecimal price;

  @Schema(description = "Business product code", example = "PRD-0001")
  private String code;

  @Schema(description = "Discounted price after promotion", example = "899.99")
  private BigDecimal discountedPrice;

  @Schema(description = "Discount percentage", example = "10")
  private Integer discountPercentage;

  @Schema(
      description = "Primary product image URL",
      example = "https://cdn.example.com/products/iphone15.jpg")
  private String image;

  @Schema(description = "Is featured product", example = "true")
  private Boolean isFeature;

  @Schema(description = "Number of times product is favorited", example = "120")
  private Integer favoritesCount;

  @Schema(description = "Category ID", example = "5")
  private Long categoryId;

  @Schema(description = "Category name", example = "Smartphones")
  private String categoryName;

  @Schema(description = "Brand information")
  private SimpleBrandResponse brand;

  @Schema(description = "Available stock quantity", example = "50")
  private Integer stockQuantity;

  @Schema(description = "Is product in stock", example = "true")
  private Boolean inStock;

  @Schema(description = "Stock status", example = "IN_STOCK")
  private StockStatus stockStatus;

  // Variants
  @Schema(description = "Whether product has multiple variants", example = "true")
  private Boolean hasVariants;

  @Schema(description = "Number of active variants", example = "4")
  private Integer activeVariantCount;

  @Schema(description = "Lowest variant price", example = "19.99")
  private BigDecimal minPrice;

  @Schema(description = "Highest variant price", example = "29.99")
  private BigDecimal maxPrice;

  @Schema(description = "Default variant SKU for quick add to cart", example = "TSHIRT-RED-M")
  private String defaultVariantSku;

  @Schema(description = "Default variant ID for quick add to cart", example = "100")
  private Long defaultVariantId;

  @Schema(description = "Grouped variant options for mobile selector UI (e.g., Color: [Red, Blue])")
  private List<VariantOptionGroup> variantOptions;

  @Schema(description = "Whether product has active promotion", example = "true")
  private Boolean hasPromotion;

  @Schema(description = "Promotion badge label", example = "10% OFF")
  private String promotionBadge;

  @Schema(description = "Quick add to cart availability", example = "true")
  private Boolean quickAddAvailable;


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Grouped variant options for mobile UI")
  public static class VariantOptionGroup {

    @Schema(description = "Attribute name (e.g., Color, Size)", example = "Color")
    private String name;

    @Schema(description = "Available values with stock info", example = "[{\"value\": \"Red\", \"inStock\": true}, {\"value\": \"Blue\", \"inStock\": false}]")
    private List<VariantOptionValue> values;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Individual variant option value")
  public static class VariantOptionValue {

    @Schema(description = "Attribute value", example = "Red")
    private String value;

    @Schema(description = "Whether any variant with this value is in stock", example = "true")
    private Boolean inStock;
  }
}
