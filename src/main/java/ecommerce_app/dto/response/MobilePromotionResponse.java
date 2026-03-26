package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Mobile promotion response")
public class MobilePromotionResponse {

  @Schema(description = "Promotion ID", example = "1")
  private Long id;

  @Schema(description = "Promotion code", example = "PROMO-001")
  private String code;

  @Schema(description = "Promotion name", example = "New Year Sale")
  private String name;

  @Schema(
      description = "Discount type",
      example = "PERCENTAGE",
      allowableValues = {"PERCENTAGE", "FIXED_AMOUNT", "BUY_X_GET_Y", "FREE_SHIPPING"})
  private String discountType;

  @Schema(description = "Discount value", example = "10")
  private BigDecimal discountValue;

  @Schema(description = "Buy quantity (for BUY_X_GET_Y)", example = "2")
  private Integer buyQuantity;

  @Schema(description = "Get quantity (for BUY_X_GET_Y)", example = "1")
  private Integer getQuantity;

  @Schema(description = "Is promotion active", example = "true")
  private Boolean active;

  @Schema(description = "Start date time", example = "2026-01-01T00:00:00")
  private LocalDateTime startAt;

  @Schema(description = "End date time", example = "2026-01-31T23:59:59")
  private LocalDateTime endAt;

  @Schema(description = "Maximum total usage", example = "1000")
  private Integer maxUsage;

  @Schema(description = "Maximum usage per user", example = "5")
  private Integer maxUsagePerUser;

  @Schema(description = "Minimum purchase amount required", example = "50.00")
  private BigDecimal minPurchaseAmount;

  // Computed fields

  @Schema(description = "Is currently valid (based on time and active flag)", example = "true")
  private Boolean isCurrentlyValid;

  @Schema(description = "Remaining usage count", example = "500")
  private Integer remainingUsage;

  @Schema(description = "Promotion status", example = "ACTIVE")
  private String status;

  // Product associations

  @Schema(description = "List of applicable products")
  private List<ProductSummary> applicableProducts;

  // Timestamps

  @Schema(description = "Created date time", example = "2026-01-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "Last updated date time", example = "2026-01-10T12:00:00")
  private LocalDateTime updatedAt;

  @Schema(description = "Created by user", example = "admin")
  private String createdBy;

  @Schema(description = "Updated by user", example = "admin")
  private String updatedBy;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Summary of product in promotion")
  public static class ProductSummary {

    @Schema(description = "Product ID", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "iPhone 15 Pro")
    private String name;

    @Schema(description = "Product code", example = "PRD-0001")
    private String code;

    @Schema(
        description = "Product image URL",
        example = "https://cdn.example.com/products/iphone.jpg")
    private String image;

    @Schema(description = "Original price", example = "999.99")
    private BigDecimal price;

    @Schema(description = "Discounted price", example = "899.99")
    private BigDecimal discountedPrice;
  }
}
