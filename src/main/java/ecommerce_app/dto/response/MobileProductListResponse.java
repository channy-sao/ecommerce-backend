package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

  @Schema(description = "Whether product has active promotion", example = "true")
  private Boolean hasPromotion;

  @Schema(description = "Promotion badge label", example = "10% OFF")
  private String promotionBadge;

  @Schema(description = "Quick add to cart availability", example = "true")
  private Boolean quickAddAvailable;
}
