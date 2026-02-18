package ecommerce_app.modules.payment.model.dto;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

// ─────────────────────────────────────────────────────
// Request: what the client sends on checkout
// ─────────────────────────────────────────────────────
@Data
public class InitiatePaymentRequest {

  @NotNull private Long orderId;

  @NotNull private PaymentGateway gateway; // BAKONG | STRIPE

  /** Stripe only: payment method ID from Stripe.js */
  private String stripePaymentMethodId;

  /** Bakong only: your app callback deeplink */
  private String appDeepLinkCallback;
}
