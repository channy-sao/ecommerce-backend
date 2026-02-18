package ecommerce_app.modules.payment.model.dto;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ─────────────────────────────────────────────────────
// Response: what the client receives to render payment UI
// ─────────────────────────────────────────────────────
@Data
@Builder
public class InitiatePaymentResponse {

  private Long paymentId;
  private PaymentGateway gateway;
  private PaymentStatus status;

  /** Bakong: Firebase deeplink URL or KHQR string to show as QR */
  private String bakongDeeplink;

  private String khqrString;

  /** Stripe: client secret for Stripe.js confirmPayment() */
  private String stripeClientSecret;

  private BigDecimal amount;
  private String currency;
  private LocalDateTime expiredAt;

  private String message;
}
