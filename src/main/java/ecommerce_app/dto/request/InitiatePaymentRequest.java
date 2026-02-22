package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.PaymentGateway;
import jakarta.validation.constraints.NotNull;
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
