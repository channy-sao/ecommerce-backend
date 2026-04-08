// Update ReceiptResponse.java
package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReceiptResponse {
  private String receiptNumber;
  private String orderNumber;
  private LocalDateTime date;
  private BigDecimal amount;
  private PaymentMethod paymentMethod;
  private String cashierName;
  private List<ReceiptItem> items;
  private BigDecimal subtotal;
  private BigDecimal discount;
  private BigDecimal shipping;
  private BigDecimal total;
  private String customerName;   // add
  private String customerEmail;  // add
  private PaymentStatus status;  // add — for the "Payment confirmed" badge
}

