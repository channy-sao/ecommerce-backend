package ecommerce_app.modules.order.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.constant.enums.ShippingMethod;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import java.math.BigDecimal;
import java.time.Instant;
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
public class OrderResponse {
  private Long id;
  private String orderNumber;
  private UserOrderResponse user;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Instant orderDate;

  private OrderStatus orderStatus;
  private PaymentStatus paymentStatus;
  private PaymentMethod paymentMethod;
  private ShippingMethod shippingMethod;

  // Financial summary
  private BigDecimal subtotalAmount;
  private BigDecimal discountAmount;
  private BigDecimal shippingCost;
  private BigDecimal shippingDiscount;
  private BigDecimal totalAmount;

  // Promotion info
  private String promotionCode;

  // Items count
  private int itemCount;

  // For GET /orders list - include minimal item info
  private List<OrderItemSimple> items;

  // For detailed view - include shipping address
  private String shippingAddress;

  // Success message for checkout
  private String message;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItemSimple {
    private ProductResponse product;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal total;
  }
}
