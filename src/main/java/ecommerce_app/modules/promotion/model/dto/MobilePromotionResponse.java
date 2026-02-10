package ecommerce_app.modules.promotion.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobilePromotionResponse {

  private Long id;
  private String code;
  private String name;
  private String discountType; // PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y, FREE_SHIPPING
  private BigDecimal discountValue;
  private Integer buyQuantity;
  private Integer getQuantity;
  private Boolean active;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private Integer maxUsage;
  private Integer maxUsagePerUser;
  private BigDecimal minPurchaseAmount;

  // Computed fields
  private Boolean isCurrentlyValid;
  private Integer remainingUsage;
  private String status; // ACTIVE, UPCOMING, EXPIRED, INACTIVE

  // Product associations
  private List<ProductSummary> applicableProducts;

  // Timestamps
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ProductSummary {
    private Long id;
    private String name;
    private String image;
    private BigDecimal price;
    private BigDecimal discountedPrice;
  }
}