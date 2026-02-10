package ecommerce_app.modules.product.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class MobileProductListResponse {

  private Long id;
  private UUID uuid;
  private String name;
  private String shortDescription;
  private BigDecimal price;
  private BigDecimal discountedPrice;
  private Integer discountPercentage;
  private String image;
  private Boolean isFeature;
  private Integer favoritesCount;

  // Minimal category info for list view
  private Long categoryId;
  private String categoryName;

  // Stock info
  private Integer stockQuantity;
  private Boolean inStock;
  private String stockStatus;

  // Promotion badges
  private Boolean hasPromotion;
  private String promotionBadge;

  // Quick actions
  private Boolean quickAddAvailable;
}
