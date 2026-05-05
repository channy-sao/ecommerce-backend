package ecommerce_app.controller;

import ecommerce_app.dto.request.BakongCallbackPayload;
import ecommerce_app.dto.response.PaymentResponse;
import ecommerce_app.dto.response.PaymentStatusResponseV2;
import ecommerce_app.entity.Order;
import ecommerce_app.service.impl.BakongKhqrService;
import ecommerce_app.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/khqr")
@RequiredArgsConstructor
public class KHQRController {

  private final BakongKhqrService bakongKhqrService;

  @PostMapping("/create/{orderId}")
  public ResponseEntity<PaymentResponse> createPayment(@PathVariable Long orderId) {
    PaymentResponse response = bakongKhqrService.createPayment(orderId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/callback")
  public ResponseEntity<PaymentStatusResponseV2> handleCallback(
      @RequestBody BakongCallbackPayload payload) {
    PaymentStatusResponseV2 response = bakongKhqrService.handleCallback(payload);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/status/{paymentId}")
  public ResponseEntity<PaymentStatusResponseV2> checkStatus(@PathVariable String paymentId) {
    PaymentStatusResponseV2 response = bakongKhqrService.checkPaymentStatus(paymentId);
    return ResponseEntity.ok(response);
  }
}
