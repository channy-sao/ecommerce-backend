// Update ReceiptResponse.java
package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.PaymentMethod;
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
  private BigDecimal tax;
  private BigDecimal total;
}

