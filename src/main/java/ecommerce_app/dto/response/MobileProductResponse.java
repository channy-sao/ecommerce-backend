package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import ecommerce_app.constant.enums.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileProductResponse {

  @Schema(description = "Database ID of the product")
  private Long id;

  @Schema(description = "UUID of the product")
  private UUID uuid;

  @Schema(description = "Full name of the product")
  private String name;

  @Schema(description = "Brand information of the product")
  private SimpleBrandResponse brand;

  @Schema(description = "Detailed description of the product")
  private String description;

  @Schema(description = "Short description or summary of the product")
  private String shortDescription;

  @Schema(description = "Original price of the product")
  private BigDecimal price;

  @Schema(description = "Discounted price of the product if any")
  private BigDecimal discountedPrice;

  @Schema(description = "Discount percentage applied on the product")
  private Integer discountPercentage;

  @Schema(description = "All product image URLs in sort order")
  private List<String> images;

  @Schema(description = "Indicates if the product is featured")
  private Boolean isFeature;

  @Schema(description = "Number of times this product was marked as favorite")
  private Integer favoritesCount;

  @Schema(description = "ID of the category this product belongs to")
  private Long categoryId;

  @Schema(description = "Name of the category this product belongs to")
  private String categoryName;

  @Schema(description = "Quantity of product in stock")
  private Integer stockQuantity;

  @Schema(description = "Indicates if the product is currently in stock")
  private Boolean inStock;

  @Schema(description = "Stock status of the product: OUT_OF_STOCK, LOW_STOCK, IN_STOCK")
  private StockStatus stockStatus;

  @Schema(description = "Indicates if the product currently has an active promotion")
  private Boolean hasPromotion;

  @Schema(description = "Badge to display for promotion, e.g., 'SALE', 'HOT'")
  private String promotionBadge;

  @Schema(description = "Active promotion details of the product")
  private PromotionDetails activePromotion;

  @Schema(description = "Indicates if the product can be added quickly to cart")
  private Boolean quickAddAvailable;

  @Schema(description = "Details of the warranty associated with the product")
  private WarrantyResponse warranty;

  @Schema(description = "Timestamp when the product was created")
  private LocalDateTime createdAt;

  @Schema(description = "Timestamp when the product was last updated")
  private LocalDateTime updatedAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Promotion details for a product")
  public static class PromotionDetails {

    @Schema(description = "ID of the promotion")
    private Long id;

    @Schema(description = "Code used for the promotion")
    private String code;

    @Schema(description = "Name of the promotion")
    private String name;

    @Schema(description = "Type of discount: PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y, FREE_SHIPPING")
    private String discountType;

    @Schema(description = "Value of discount (percentage or fixed amount)")
    private BigDecimal discountValue;

    @Schema(description = "Quantity to buy for BUY_X_GET_Y type promotion")
    private Integer buyQuantity;

    @Schema(description = "Quantity to get for BUY_X_GET_Y type promotion")
    private Integer getQuantity;

    @Schema(description = "Start datetime of the promotion")
    private LocalDateTime startAt;

    @Schema(description = "End datetime of the promotion")
    private LocalDateTime endAt;

    @Schema(description = "Minimum purchase amount to apply the promotion")
    private BigDecimal minPurchaseAmount;

    @Schema(description = "Remaining usage count of the promotion")
    private Integer remainingUsage;
  }
}
