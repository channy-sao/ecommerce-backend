package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.PromotionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "PromotionResponse", description = "Response payload for promotion details")
public class PromotionResponse {

  @Schema(description = "Promotion unique identifier", example = "1")
  private Long id;

  @Schema(description = "Promotion code", example = "SUMMER2026")
  private String code;

  @Schema(description = "Promotion display name", example = "Summer Sale 2026")
  private String name;

  @Schema(description = "Type of promotion (PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y)")
  private PromotionType discountType;

  @Schema(description = "Discount value (percentage or fixed amount)", example = "10.0")
  private BigDecimal discountValue;

  @Schema(description = "Buy quantity for BUY_X_GET_Y type", example = "2")
  private Integer buyQuantity;

  @Schema(description = "Free quantity for BUY_X_GET_Y type", example = "1")
  private Integer getQuantity;

  @Schema(description = "Indicates if promotion is active", example = "true")
  private Boolean active;

  @Schema(description = "Promotion start date", example = "2026-06-01T00:00:00")
  private LocalDateTime startAt;

  @Schema(description = "Promotion end date", example = "2026-06-30T23:59:59")
  private LocalDateTime endAt;

  @Schema(description = "Maximum usage allowed", example = "100")
  private Integer maxUsage;

  @Schema(description = "Minimum purchase amount required", example = "50.00")
  private BigDecimal minPurchaseAmount;

  @Schema(description = "Current number of times this promotion has been used", example = "25")
  private Integer currentUsage;

  @Schema(description = "Products this promotion applies to (null if applyToAll = true)")
  private List<SimpleProductResponse> products;

  @Schema(description = "Indicates if promotion applies to all products", example = "false")
  private boolean applyToAll;

  @Schema(description = "Promotion creation timestamp", example = "2026-05-01T10:15:30")
  private LocalDateTime createdAt;

  @Schema(description = "Last updated timestamp", example = "2026-05-10T09:45:00")
  private LocalDateTime updatedAt;
}
