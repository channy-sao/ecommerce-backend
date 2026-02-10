package ecommerce_app.modules.product.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
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
@Schema(name = "MobileProductDetailResponse", description = "Complete product details for mobile")
public class MobileProductDetailResponse {

  // Basic Info (same as list view for consistency)
  @Schema(description = "Product ID", example = "1")
  private Long id;

  @Schema(description = "Product UUID")
  private UUID uuid;

  @Schema(description = "Product name", example = "iPhone 15 Pro")
  private String name;

  @Schema(
      description = "Full description",
      example = "Latest Apple smartphone with A17 Pro chip...")
  private String description;

  @Schema(description = "Product price", example = "1299.99")
  private BigDecimal price;

  @Schema(description = "Discounted price if available", example = "1199.99")
  private BigDecimal discountedPrice;

  @Schema(description = "Discount percentage", example = "15")
  private Integer discountPercentage;

  @Schema(description = "Main product image")
  private String mainImage;

  @Schema(description = "Additional product images")
  private List<String> galleryImages;

  // Stock & Availability
  @Schema(description = "Stock quantity", example = "50")
  private Integer stockQuantity;

  @Schema(description = "Stock status", example = "IN_STOCK")
  private String stockStatus;

  @Schema(description = "Maximum allowed quantity per order", example = "10")
  private Integer maxQuantity;

  @Schema(description = "Minimum order quantity", example = "1")
  private Integer minQuantity;

  // Category
  @Schema(description = "Category ID", example = "1")
  private Long categoryId;

  @Schema(description = "Category name", example = "Electronics")
  private String categoryName;

  // Features
  @Schema(description = "Whether product is featured", example = "true")
  private Boolean isFeature;

  @Schema(description = "Favorites count", example = "245")
  private Integer favoritesCount;

  @Schema(description = "Whether current user has favorited", example = "false")
  private Boolean isFavorited;

  // Social Proof
  @Schema(description = "Average rating", example = "4.5")
  private Double averageRating;

  @Schema(description = "Total reviews count", example = "1250")
  private Integer reviewCount;

  // Promotion Details
  @Schema(description = "Active promotions")
  private List<PromotionDetail> activePromotions;

  // Additional Info
  @Schema(description = "Created timestamp")
  private Instant createdAt;

  @Schema(description = "Product specifications")
  private List<ProductSpecification> specifications;

  @Schema(description = "Product tags")
  private List<String> tags;

  @Schema(description = "Estimated delivery days", example = "3")
  private Integer estimatedDeliveryDays;

  @Schema(description = "Return policy")
  private String returnPolicy;
}

// Supporting DTOs
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDetail {
  @Schema(description = "Promotion ID", example = "1")
  private Long id;

  @Schema(description = "Promotion name", example = "Summer Sale")
  private String name;

  @Schema(description = "Discount value", example = "15% OFF")
  private String discountText;

  @Schema(description = "End time")
  private LocalDateTime endTime;
}

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProductSpecification {
  @Schema(description = "Specification key", example = "Processor")
  private String key;

  @Schema(description = "Specification value", example = "A17 Pro")
  private String value;
}
