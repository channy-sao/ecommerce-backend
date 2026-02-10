package ecommerce_app.modules.product.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileProductResponse {

  private Long id;
  private UUID uuid;
  private String name;
  private String description;
  private String shortDescription;
  private BigDecimal price;
  private BigDecimal discountedPrice;
  private Integer discountPercentage;
  private String image;
  private Boolean isFeature;
  private Integer favoritesCount;

  // Category info
  private Long categoryId;
  private String categoryName;

  // Stock info
  private Integer stockQuantity;
  private Boolean inStock;
  private String stockStatus; // OUT_OF_STOCK, LOW_STOCK, IN_STOCK

  // Promotion info
  private Boolean hasPromotion;
  private String promotionBadge;
  private PromotionDetails activePromotion;

  // Quick actions
  private Boolean quickAddAvailable;

  // Timestamps
  private Instant createdAt;
  private Instant updatedAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class PromotionDetails {
    private Long id;
    private String code;
    private String name;
    private String discountType; // PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y, FREE_SHIPPING
    private BigDecimal discountValue;
    private Integer buyQuantity;
    private Integer getQuantity;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BigDecimal minPurchaseAmount;
    private Integer remainingUsage;
  }
}