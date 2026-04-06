// Update PaymentTransactionResponse.java
package ecommerce_app.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentTransactionResponse {
  private Long id;
  private String referenceNumber;
  private BigDecimal amount;
  private String paymentMethod;
  private String transactionType;
  private String status;
  private String cashierName;
  private LocalDateTime transactionDate;
  private String notes;
  private Long orderId;
  private String orderNumber;
}
