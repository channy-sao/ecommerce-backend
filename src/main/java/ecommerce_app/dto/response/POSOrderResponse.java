package ecommerce_app.dto.response;

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
public class POSOrderResponse {
  private Long orderId;
  private String orderNumber;
  private LocalDateTime orderDate;
  private BigDecimal subtotal;
  private BigDecimal discountAmount;
  private BigDecimal totalAmount;
  private String paymentMethod;
  private String paymentStatus;
  private String orderStatus;
  private String receiptNumber;
  private String cashierName;
  private List<POSOrderItemResponse> items;
}
