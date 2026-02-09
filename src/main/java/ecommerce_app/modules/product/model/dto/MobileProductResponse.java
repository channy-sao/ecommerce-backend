package ecommerce_app.modules.product.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "MobileProductListResponse",
    description = "Lightweight product response for mobile list view")
public class MobileProductResponse {

  @Schema(description = "Product ID", example = "1")
  private Long id;

  @Schema(description = "Product UUID")
  private UUID uuid;

  @Schema(description = "Product name", example = "iPhone 15 Pro")
  private String name;

  @Schema(
      description = "Short description (truncated for list view)",
      example = "Latest Apple smartphone...")
  private String shortDescription;

  @Schema(description = "Product price", example = "1299.99")
  private BigDecimal price;

  @Schema(
      description = "Product image URL",
      example = "https://cdn.example.com/products/iphone15.jpg")
  private String image;

  @Schema(description = "Whether product is featured", example = "true")
  private Boolean isFeature;

  @Schema(description = "Number of favorites", example = "245")
  private Integer favoritesCount;

  @Schema(description = "Stock quantity available", example = "50")
  private Integer stockQuantity;

  @Schema(description = "Whether product is in stock", example = "true")
  private Boolean inStock;

  @Schema(description = "Category ID", example = "1")
  private Long categoryId;

  @Schema(description = "Category name", example = "Electronics")
  private String categoryName;
}
