package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Order item response payload")
public class OrderItemResponse {

  @Schema(description = "Unique order item ID", example = "1")
  private Long id;

  @Schema(description = "Product details for this order item")
  private ProductResponse product;

  @Schema(description = "Quantity ordered", example = "2")
  private Integer quantity;

  @Schema(description = "Unit price at time of purchase", example = "49.99")
  private BigDecimal originalPrice;

  @Schema(description = "Subtotal before discount (originalPrice × quantity)", example = "99.98")
  private BigDecimal subtotal;

  @Schema(description = "Discount amount applied to this item", example = "10.00")
  private BigDecimal discountAmount;

  @Schema(
      description = "Final total for this item after discount (subtotal - discountAmount)",
      example = "89.98")
  private BigDecimal totalPrice;

  @Schema(description = "Promotion code applied to this item", example = "SUMMER25")
  private String promotionCode;

  @Schema(description = "Discount percentage applied to this item", example = "10.00")
  private BigDecimal discountPercentage;

  public BigDecimal getDiscountPercentage() {
    if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return discountAmount
        .multiply(BigDecimal.valueOf(100))
        .divide(subtotal, 2, java.math.RoundingMode.HALF_UP);
  }
}
