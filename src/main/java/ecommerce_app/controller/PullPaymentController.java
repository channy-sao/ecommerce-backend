package ecommerce_app.controller;

import ecommerce_app.dto.request.PullPaymentRequest;
import ecommerce_app.dto.response.PullPaymentResponse;
import ecommerce_app.dto.response.PullPaymentStatusResponse;
import ecommerce_app.service.impl.BakongPullPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/pull")
@RequiredArgsConstructor
public class PullPaymentController {

  private final BakongPullPaymentService pullPaymentService;

  /** Cashier/Admin initiates pull payment from customer */
  @PostMapping("/request")
  public ResponseEntity<PullPaymentResponse> requestPayment(
      @RequestBody PullPaymentRequest request) {
    PullPaymentResponse response = pullPaymentService.initiatePullPayment(request);
    return ResponseEntity.ok(response);
  }

  /** Check payment status (for polling by cashier UI) */
  @GetMapping("/status/{paymentId}")
  public ResponseEntity<PullPaymentStatusResponse> checkStatus(@PathVariable String paymentId) {
    PullPaymentStatusResponse response = pullPaymentService.checkPullPaymentStatus(paymentId);
    return ResponseEntity.ok(response);
  }

  /** Cancel payment request */
  @PostMapping("/cancel/{paymentId}")
  public ResponseEntity<Void> cancelPayment(@PathVariable String paymentId) {
    pullPaymentService.cancelPullPayment(paymentId);
    return ResponseEntity.ok().build();
  }
}
