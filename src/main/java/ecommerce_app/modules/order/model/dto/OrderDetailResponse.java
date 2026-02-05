package ecommerce_app.modules.order.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.constant.enums.ShippingMethod;
import ecommerce_app.modules.address.model.dto.AddressResponse;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponse {
  private Long id;
  private String orderNumber;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Instant orderDate;

  // Status
  private OrderStatus orderStatus;
  private PaymentStatus paymentStatus;

  // Payment & Shipping
  private PaymentMethod paymentMethod;
  private ShippingMethod shippingMethod;
  private BigDecimal shippingCost;

  // Financial breakdown
  private FinancialBreakdown financialBreakdown;

  // Items
  private List<OrderItemDetail> items;

  // Address (parsed from snapshot)
  private AddressResponse shippingAddress;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<OrderStatusHistoryResponse> statusHistories;

  // Helper methods
  public int getItemCount() {
    return items != null ? items.size() : 0;
  }

  public BigDecimal getTotalAmount() {
    return financialBreakdown != null ? financialBreakdown.getTotal() : BigDecimal.ZERO;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FinancialBreakdown {
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shipping;
    private BigDecimal total;

    // For display
    public BigDecimal getSavings() {
      return discount != null ? discount : BigDecimal.ZERO;
    }

    public BigDecimal getSavingsPercentage() {
      if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) == 0) {
        return BigDecimal.ZERO;
      }
      return getSavings()
          .multiply(BigDecimal.valueOf(100))
          .divide(subtotal, 2, java.math.RoundingMode.HALF_UP);
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItemDetail {
    private Long id;
    private ProductResponse product;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;

    // For display
    public BigDecimal getDiscountPercentage() {
      if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) == 0) {
        return BigDecimal.ZERO;
      }
      if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) {
        return BigDecimal.ZERO;
      }
      return discount
          .multiply(BigDecimal.valueOf(100))
          .divide(subtotal, 2, java.math.RoundingMode.HALF_UP);
    }
  }
}
