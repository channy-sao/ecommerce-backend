package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.entity.KHQRPayment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

  private String paymentId;
  private Long orderId;
  private String qrString;
  private String deepLink;
  private BigDecimal amount;
  private String currency;
  private PaymentStatus status;
  private Instant expiresAt;
  private LocalDateTime createdAt;

  // Factory method: build from entity
  public static PaymentResponse from(KHQRPayment payment) {
    PaymentResponse res = new PaymentResponse();
    res.paymentId = payment.getId();
    res.orderId = payment.getOrder().getId();
    res.qrString = payment.getQrString();
    res.deepLink = payment.getDeepLink();
    res.amount = payment.getAmount();
    res.currency = payment.getCurrency();
    res.status = payment.getStatus();
    res.expiresAt = payment.getExpiresAt();
    res.createdAt = payment.getCreatedAt();
    return res;
  }
}
