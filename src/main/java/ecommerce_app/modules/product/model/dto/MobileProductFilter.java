package ecommerce_app.modules.product.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "MobileProductFilter", description = "Filter criteria for mobile product listing")
public class MobileProductFilter {

  @Schema(description = "Search keyword", example = "iphone 15", maxLength = 100)
  private String keyword;

  @Schema(description = "Category ID", example = "1")
  private Long categoryId;

  @Schema(description = "Minimum price", example = "100.00", minimum = "0.00")
  @PositiveOrZero(message = "Minimum price must be zero or positive")
  private BigDecimal minPrice;

  @Schema(description = "Maximum price", example = "2000.00", minimum = "0.00")
  @PositiveOrZero(message = "Maximum price must be zero or positive")
  private BigDecimal maxPrice;

  @Schema(description = "Show only featured products", example = "false")
  private Boolean featuredOnly;

  @Schema(description = "Show only in-stock products", example = "true")
  private Boolean inStockOnly;

  @Schema(description = "Show only products with active promotions", example = "false")
  private Boolean withPromotionsOnly;

  @Schema(
      description = "Sort by field",
      example = "PRICE",
      allowableValues = {
        "PRICE",
        "NAME",
        "FAVORITES",
        "CREATED_AT",
        "DISCOUNT",
        "RATING",
        "POPULARITY"
      })
  private SortField sortBy;

  @Schema(
      description = "Sort direction",
      example = "DESC",
      allowableValues = {"ASC", "DESC"})
  private Sort.Direction sortDirection;

  @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0", minimum = "0")
  @Min(value = 0, message = "Page must be 0 or greater")
  private Integer page = 0;

  @Schema(
      description = "Page size",
      example = "20",
      defaultValue = "20",
      minimum = "1",
      maximum = "100")
  @Min(value = 1, message = "Page size must be at least 1")
  @Max(value = 100, message = "Page size cannot exceed 100")
  private Integer size = 20;

  @Schema(
      description = "Filter by multiple categories (takes precedence over categoryId)",
      example = "[1, 2, 3]")
  private List<Long> categoryIds;

  @Schema(description = "Minimum rating (0-5)", example = "4.0", minimum = "0", maximum = "5")
  @Min(value = 0, message = "Minimum rating must be between 0 and 5")
  @Max(value = 5, message = "Minimum rating must be between 0 and 5")
  private Double minRating;

  @Schema(description = "User ID for personalized recommendations", example = "123")
  private Long userId;

  @Schema(description = "Filter by product tags", example = "[\"smartphone\", \"apple\", \"5g\"]")
  private List<String> tags;

  @Schema(description = "Exclude specific product IDs", example = "[999, 1000]")
  private List<Long> excludeProductIds;

  @Schema(description = "Maximum delivery days", example = "7")
  @Min(value = 0, message = "Delivery days must be zero or positive")
  private Integer maxDeliveryDays;

  @Schema(
      description = "Filter by availability status",
      example = "IN_STOCK",
      allowableValues = {"IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK", "ALL"})
  private StockStatusFilter stockStatus;

  @Schema(
      description = "Minimum discount percentage",
      example = "10",
      minimum = "0",
      maximum = "100")
  @Min(value = 0, message = "Discount percentage must be between 0 and 100")
  @Max(value = 100, message = "Discount percentage must be between 0 and 100")
  private Integer minDiscountPercentage;

  @Schema(
      description = "Whether to include deleted products (admin only)",
      example = "false",
      hidden = true)
  private Boolean includeDeleted;

  @Schema(description = "Filter by creation date range - from", example = "2024-01-01T00:00:00")
  private String createdFrom;

  @Schema(description = "Filter by creation date range - to", example = "2024-12-31T23:59:59")
  private String createdTo;

  // Enums for better type safety
  public enum SortField {
    PRICE,
    NAME,
    FAVORITES,
    CREATED_AT,
    DISCOUNT,
    RATING,
    POPULARITY
  }

  public enum StockStatusFilter {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK,
    ALL
  }

  // Validation method
  public void validate() {
    if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
      throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
    }

    if (size > 100) {
      size = 100; // Cap at 100 for mobile performance
    }
  }

  // Helper methods
  public boolean hasKeyword() {
    return keyword != null && !keyword.trim().isEmpty();
  }

  public boolean hasCategoryFilter() {
    return categoryId != null || (categoryIds != null && !categoryIds.isEmpty());
  }

  public boolean hasPriceFilter() {
    return minPrice != null || maxPrice != null;
  }

  public boolean hasRatingFilter() {
    return minRating != null;
  }

  public boolean hasStockStatusFilter() {
    return stockStatus != null && stockStatus != StockStatusFilter.ALL;
  }

  public boolean hasDiscountFilter() {
    return minDiscountPercentage != null;
  }

  public boolean hasDateFilter() {
    return createdFrom != null || createdTo != null;
  }
}
