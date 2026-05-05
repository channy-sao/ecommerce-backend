package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.entity.KHQRPayment;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponseV2 {
  private String paymentId;
  private Long orderId;
  private PaymentStatus status;
  private Instant paidAt;
  private Instant expiresAt;
  private String bakongTransactionId;

  public static PaymentStatusResponseV2 from(KHQRPayment payment) {
    PaymentStatusResponseV2 res = new PaymentStatusResponseV2();
    res.paymentId = payment.getId();
    res.orderId = payment.getOrder().getId();
    res.status = payment.getStatus();
    res.paidAt = payment.getPaidAt();
    res.expiresAt = payment.getExpiresAt();
    res.bakongTransactionId = payment.getBakongTransactionId();
    return res;
  }
}
