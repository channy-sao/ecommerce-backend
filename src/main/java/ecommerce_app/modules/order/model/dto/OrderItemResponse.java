package ecommerce_app.modules.order.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.modules.product.model.dto.ProductResponse;
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
public class OrderItemResponse {
  private Long id;
  private ProductResponse product;
  private Integer quantity;

  // Price breakdown
  private BigDecimal originalPrice; // Price per unit at time of purchase
  private BigDecimal subtotal; // originalPrice * quantity
  private BigDecimal discountAmount;
  private BigDecimal totalPrice; // subtotal - discountAmount

  // Promotion info for this item
  private String promotionCode;
  private BigDecimal discountPercentage;

  // Helper method
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
