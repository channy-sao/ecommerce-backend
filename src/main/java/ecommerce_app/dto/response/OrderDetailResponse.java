package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.constant.enums.ShippingMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
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
@Schema(description = "Detailed order response payload")
public class OrderDetailResponse {

  @Schema(description = "Unique order ID", example = "1")
  private Long id;

  @Schema(description = "Human-readable order number", example = "ORD-20240101-0001")
  private String orderNumber;

  @Schema(description = "Date and time the order was placed", example = "2024-01-01 10:00:00")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime orderDate;

  @Schema(description = "Current status of the order", example = "PENDING")
  private OrderStatus orderStatus;

  @Schema(description = "Payment status of the order", example = "PAID")
  private PaymentStatus paymentStatus;

  @Schema(description = "Payment method used", example = "COD")
  private PaymentMethod paymentMethod;

  @Schema(description = "Payment gateway resolved from payment method", example = "COD")
  private PaymentGateway paymentGateway; // ← added

  @Schema(description = "Shipping method selected", example = "STANDARD")
  private ShippingMethod shippingMethod;

  @Schema(description = "Shipping cost for the order", example = "5.00")
  private BigDecimal shippingCost;

  @Schema(description = "Financial breakdown of the order")
  private FinancialBreakdown financialBreakdown;

  @Schema(description = "List of items in the order")
  private List<OrderItemDetail> items;

  @Schema(description = "Parsed shipping address for the order")
  private AddressResponse shippingAddress;

  @Schema(description = "Order status change history")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<OrderStatusHistoryResponse> statusHistories;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Financial breakdown of order costs")
  public static class FinancialBreakdown {

    @Schema(description = "Subtotal before discount and shipping", example = "99.99")
    private BigDecimal subtotal;

    @Schema(description = "Total discount applied", example = "10.00")
    private BigDecimal discount;

    @Schema(description = "Shipping cost", example = "5.00")
    private BigDecimal shipping;

    @Schema(description = "Final total amount", example = "94.99")
    private BigDecimal total;

    public BigDecimal getSavings() {
      return discount != null ? discount : BigDecimal.ZERO;
    }

    public BigDecimal getSavingsPercentage() {
      if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
      return getSavings()
          .multiply(BigDecimal.valueOf(100))
          .divide(subtotal, 2, java.math.RoundingMode.HALF_UP);
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Individual order item detail")
  public static class OrderItemDetail {

    @Schema(description = "Unique order item ID", example = "1")
    private Long id;

    @Schema(description = "Product details for this order item")
    private ProductResponse product;

    @Schema(description = "Unit price of the product at time of order", example = "49.99")
    private BigDecimal unitPrice;

    @Schema(description = "Quantity ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Subtotal before discount (unitPrice × quantity)", example = "99.98")
    private BigDecimal subtotal;

    @Schema(description = "Discount applied to this line item", example = "10.00")
    private BigDecimal discount;

    @Schema(description = "Final total for this line item after discount", example = "89.98")
    private BigDecimal total;

    public BigDecimal getDiscountPercentage() {
      if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
      if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
      return discount
          .multiply(BigDecimal.valueOf(100))
          .divide(subtotal, 2, java.math.RoundingMode.HALF_UP);
    }
  }
}
