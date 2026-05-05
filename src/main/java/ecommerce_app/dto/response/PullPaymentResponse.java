package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullPaymentResponse {
  private String paymentId;
  private String transactionId; // Bakong transaction ID
  private String customerPhone;
  private BigDecimal amount;
  private String currency;
  private PaymentStatus status;
  private String statusMessage;
  private Instant createdAt;
  private Instant expiresAt;
  private Instant paidAt;
  private String orderNumber;
  private String merchantName;
  private String cashierName;
  private String storeName;
}
