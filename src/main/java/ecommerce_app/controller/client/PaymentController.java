package ecommerce_app.controller.client;

import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.request.InitiatePaymentRequest;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.InitiatePaymentResponse;
import ecommerce_app.dto.response.PaymentStatusResponse;
import ecommerce_app.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Client payment process")
public class PaymentController {

  private final PaymentService paymentService;

  /**
   * Step 1: Initiate payment after order is created. Call this right after checkout with the
   * orderId and chosen gateway.
   *
   * <p>POST /api/client/v1/payments/initiate
   */
  @PostMapping("/initiate")
  @Operation(summary = "Initiate payment for an order")
  public ResponseEntity<BaseBodyResponse<InitiatePaymentResponse>> initiatePayment(
      @RequestBody @Valid InitiatePaymentRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    InitiatePaymentResponse response = paymentService.initiate(request, userDetails.getId());
    return BaseBodyResponse.success(response, "Payment initiated successfully");
  }

  /**
   * Step 2: Poll payment status (used for KHQR QR polling). COD and Cash-in-Shop can also use this
   * to check current state.
   *
   * <p>GET /api/client/v1/payments/{paymentId}/status
   */
  @GetMapping("/{paymentId}/status")
  @Operation(summary = "Get payment status")
  public ResponseEntity<BaseBodyResponse<PaymentStatusResponse>> getPaymentStatus(
      @PathVariable Long paymentId, @AuthenticationPrincipal CustomUserDetails userDetails) {

    PaymentStatusResponse response = paymentService.getStatus(paymentId, userDetails.getId());
    return BaseBodyResponse.success(response, "Payment status retrieved");
  }
}
