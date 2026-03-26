package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.OrderStatus;
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
@Schema(description = "Order response payload")
public class OrderResponse {

  @Schema(description = "Unique order ID", example = "1")
  private Long id;

  @Schema(description = "Human-readable order number", example = "ORD-20240101-0001")
  private String orderNumber;

  @Schema(description = "User who placed the order")
  private UserOrderResponse user;

  @Schema(description = "Date and time the order was placed", example = "2024-01-01T10:00:00")
  private LocalDateTime orderDate;

  @Schema(description = "Current status of the order", example = "PENDING")
  private OrderStatus orderStatus;

  @Schema(description = "Payment status of the order", example = "PAID")
  private PaymentStatus paymentStatus;

  @Schema(description = "Payment method used", example = "CREDIT_CARD")
  private PaymentMethod paymentMethod;

  @Schema(description = "Shipping method selected", example = "STANDARD")
  private ShippingMethod shippingMethod;

  // Financial summary
  @Schema(description = "Subtotal before discounts and shipping", example = "99.99")
  private BigDecimal subtotalAmount;

  @Schema(description = "Total discount applied", example = "10.00")
  private BigDecimal discountAmount;

  @Schema(description = "Shipping cost", example = "5.00")
  private BigDecimal shippingCost;

  @Schema(description = "Discount applied to shipping", example = "5.00")
  private BigDecimal shippingDiscount;

  @Schema(description = "Final total amount charged", example = "94.99")
  private BigDecimal totalAmount;

  // Promotion info
  @Schema(description = "Promotion code applied to the order", example = "SUMMER25")
  private String promotionCode;

  // Items count
  @Schema(description = "Total number of items in the order", example = "3")
  private int itemCount;

  // For GET /orders list - include minimal item info
  @Schema(description = "List of items in the order")
  private List<OrderItemSimple> items;

  // For a detailed view - include shipping address
  @Schema(
      description = "Shipping address for the order",
      example = "123 Main St, Phnom Penh, Cambodia")
  private String shippingAddress;

  // Success message for checkout
  @Schema(description = "Success or informational message", example = "Order placed successfully")
  private String message;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Simplified order item")
  public static class OrderItemSimple {

    @Schema(description = "Product details")
    private ProductResponse product;

    @Schema(description = "Quantity ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price at time of order", example = "49.99")
    private BigDecimal price;

    @Schema(description = "Total price for this line item", example = "99.98")
    private BigDecimal total;
  }
}
