package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.PromotionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Mobile promotion list response")
public class MobilePromotionListResponse {

  @Schema(description = "Promotion ID", example = "1")
  private Long id;

  @Schema(description = "Promotion code", example = "PROMO-001")
  private String code;

  @Schema(description = "Promotion name", example = "New Year Sale")
  private String name;

  @Schema(description = "Discount type", example = "PERCENTAGE")
  private PromotionType discountType;

  @Schema(description = "Discount value", example = "20")
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

  @Schema(description = "Minimum purchase amount required", example = "50.00")
  private BigDecimal minPurchaseAmount;

  // Display fields

  @Schema(description = "Display text for UI", example = "20% OFF")
  private String displayText;

  @Schema(description = "Promotion status", example = "ACTIVE")
  private String status;

  @Schema(description = "Is currently valid", example = "true")
  private Boolean isCurrentlyValid;

  @Schema(description = "Remaining usage count", example = "500")
  private Integer remainingUsage;

  // Product count

  @Schema(description = "Number of applicable products", example = "25")
  private Integer applicableProductsCount;
}
