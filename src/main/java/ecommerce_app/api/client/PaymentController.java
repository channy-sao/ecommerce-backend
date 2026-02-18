package ecommerce_app.api.client;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.payment.model.dto.InitiatePaymentRequest;
import ecommerce_app.modules.payment.model.dto.InitiatePaymentResponse;
import ecommerce_app.modules.payment.model.dto.PaymentStatusResponse;
import ecommerce_app.modules.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Payment REST API.
 *
 * <p>─── Typical client flow ─────────────────────────────────────────────────────
 *
 * <p>1. User fills cart → clicks "Checkout" POST /api/orders/checkout → returns { orderId, ... }
 *
 * <p>2. Client immediately calls initiate payment (forced redirect to payment): POST
 * /api/payments/initiate { "orderId": 123, "gateway": "BAKONG" | "STRIPE", ... } ← { paymentId,
 * bakongDeeplink | stripeClientSecret, expiredAt, ... }
 *
 * <p>3a. Bakong: Client renders QR code (bakongDeeplink) or opens deeplink. Client polls GET
 * /api/payments/123/status every 3 seconds until PAID/FAILED.
 *
 * <p>3b. Stripe: Client calls stripe.confirmPayment(stripeClientSecret, paymentMethodId). Stripe
 * redirects back to your site; webhook fires payment_intent.succeeded. Client can also poll GET
 * /api/payments/123/status as a fallback.
 *
 * <p>4. On PAID → redirect to order confirmation page. On FAILED → show error, allow retry (POST
 * /api/payments/initiate again).
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/client/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  /**
   * Step 2: Initiate payment after checkout. Client must call this immediately after receiving
   * orderId from checkout.
   *
   * <p>POST /api/payments/initiate
   */
  @PostMapping("/initiate")
  public ResponseEntity<BaseBodyResponse<InitiatePaymentResponse>> initiate(
      @Valid @RequestBody InitiatePaymentRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    InitiatePaymentResponse response = paymentService.initiate(request, user.getId());
    return BaseBodyResponse.success(response, "Payment initiated successfully");
  }

  /**
   * Step 3: Poll payment status. Client calls this every 3 seconds while showing payment QR /
   * waiting screen. Stop polling when status = PAID or FAILED.
   *
   * <p>GET /api/payments/{paymentId}/status
   */
  @GetMapping("/{paymentId}/status")
  public ResponseEntity<BaseBodyResponse<PaymentStatusResponse>> getStatus(
      @PathVariable Long paymentId, @AuthenticationPrincipal Long userId) {

    PaymentStatusResponse response = paymentService.getStatus(paymentId, userId);
    return BaseBodyResponse.success(response, "Payment status retrieved successfully");
  }
}
