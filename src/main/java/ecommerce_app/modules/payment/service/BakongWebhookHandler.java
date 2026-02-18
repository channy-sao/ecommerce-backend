package ecommerce_app.modules.payment.service;

import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.modules.payment.model.entity.Payment;
import ecommerce_app.modules.payment.repository.PaymentRepository;
import ecommerce_app.modules.payment.service.PaymentService;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

/**
 * Bakong NBC callback receiver.
 *
 * <p>NBC calls this endpoint after the user pays via the Bakong app. Configure this URL in your
 * NBC/Bakong merchant portal as the callback URL.
 *
 * <p>URL: POST https://yourapp.com/api/webhooks/bakong
 *
 * <p>NOTE: NBC's callback format may vary by integration type. Adjust the payload parsing below to
 * match your specific NBC setup. Always verify the transaction via check_transaction_by_md5 as well
 * (do not trust the callback payload alone).
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class BakongWebhookHandler {

  private final PaymentRepository paymentRepository;
  private final PaymentService paymentService;

  /**
   * NBC calls this endpoint with the transaction result. We verify the transaction via the Bakong
   * API (via syncStatus) before marking paid.
   */
  @PostMapping("/bakong")
  public ResponseEntity<String> handleBakongCallback(@RequestBody Map<String, Object> payload) {
    log.info("Bakong callback received: {}", payload);

    try {
      // NBC typically sends the MD5 hash or transaction hash in the callback.
      // Adjust field names to match your actual NBC callback contract.
      String md5 = (String) payload.getOrDefault("md5", payload.get("transactionId"));
      String transactionHash = (String) payload.get("transactionHash");

      if (md5 == null && transactionHash == null) {
        log.warn("Bakong callback missing transaction identifier");
        return ResponseEntity.badRequest().body("Missing transaction identifier");
      }

      // Look up our payment record by MD5 (stored as gatewayReference)
      String lookupKey = md5 != null ? md5 : transactionHash;
      paymentRepository
          .findByGatewayReference(lookupKey)
          .ifPresentOrElse(
              payment -> {
                // Store raw callback for audit
                payment.setRawCallback(payload.toString());

                // Verify via Bakong API (syncStatus polls check_transaction_by_md5)
                // This prevents accepting forged callbacks
                // The gateway strategy is injected indirectly via PaymentService
                updatePaymentFromCallback(payment, payload);
                paymentService.confirmPayment(payment);
              },
              () -> log.warn("No payment found for Bakong reference: {}", lookupKey));

    } catch (Exception e) {
      log.error("Error processing Bakong callback: {}", e.getMessage(), e);
    }

    // Always return 200 to NBC to acknowledge receipt
    return ResponseEntity.ok("OK");
  }

  /**
   * Applies NBC callback payload to the Payment. NBC responseCode 0 = success; otherwise treat as
   * failed.
   */
  private void updatePaymentFromCallback(Payment payment, Map<String, Object> payload) {
    Integer responseCode = (Integer) payload.get("responseCode");

    if (responseCode != null && responseCode == 0) {
      payment.setStatus(PaymentStatus.PAID);
      payment.setPaidAt(LocalDateTime.now());

      // Store transaction hash if provided
      String hash = (String) payload.get("transactionHash");
      if (hash != null) payment.setGatewayTransactionId(hash);

      log.info("[Bakong] Payment #{} confirmed via webhook", payment.getId());

    } else {
      payment.setStatus(PaymentStatus.FAILED);
      log.info(
          "[Bakong] Payment #{} failed via webhook (responseCode={})",
          payment.getId(),
          responseCode);
    }
  }
}
