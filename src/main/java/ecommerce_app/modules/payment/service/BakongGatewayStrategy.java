package ecommerce_app.modules.payment.service;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.payment.model.dto.InitiatePaymentRequest;
import ecommerce_app.modules.payment.model.entity.Payment;
import java.time.LocalDateTime;
import java.util.Map;

import ecommerce_app.modules.payment.strategy.PaymentGatewayStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Bakong (NBC Cambodia) KHQR gateway.
 *
 * <p>Flow: 1. Your backend generates / receives a KHQR string (from your KHQR SDK or NBC API). 2.
 * We call Bakong's generate_deeplink_by_qr to get a Firebase deeplink. 3. Client opens the deeplink
 * → user pays in Bakong app. 4. We poll check_transaction_by_md5 until PAID or EXPIRED. 5. Webhook
 * endpoint also receives confirmation from NBC (backup).
 *
 * <p>NOTE: KHQR string generation is application-specific. You may use the `khqr-sdk` library from
 * NBC or generate via your bank partner. This class expects the KHQR string already embedded in the
 * order or generated here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BakongGatewayStrategy implements PaymentGatewayStrategy {

  private final RestTemplate restTemplate;

  @Value("${bakong.api.base-url}")
  private String bakongBaseUrl;

  @Value("${bakong.api.token}")
  private String bakongToken;

  @Value("${bakong.app.icon-url}")
  private String appIconUrl;

  @Value("${bakong.app.name}")
  private String appName;

  // QR expires after 5 minutes (NBC best practice)
  private static final int QR_TTL_MINUTES = 5;

  // ─── Strategy identity ────────────────────────────────
  @Override
  public PaymentGateway getGateway() {
    return PaymentGateway.BAKONG;
  }

  // ─── Step 1: Initiate payment ─────────────────────────
  @Override
  public Payment initiate(Order order, InitiatePaymentRequest request) {
    log.info("[Bakong] Initiating payment for order #{}", order.getOrderNumber());

    // 1. Build KHQR string for this order
    //    Replace this with your actual KHQR SDK / bank partner call.
    String khqrString = buildKhqrString(order);

    // 2. Compute MD5 of the KHQR string — used for status polling
    String md5 = computeMd5(khqrString);

    // 3. Call NBC deeplink API
    String deeplink =
        generateDeeplink(
            khqrString,
            request.getAppDeepLinkCallback() != null
                ? request.getAppDeepLinkCallback()
                : "https://yourapp.com/payment/callback");

    // 4. Build Payment entity (not yet persisted)
    return Payment.builder()
        .order(order)
        .gateway(PaymentGateway.BAKONG)
        .gatewayReference(md5) // used for polling
        .paymentUrl(deeplink) // sent to client
        .amount(order.getTotalAmount())
        .currency("USD")
        .status(PaymentStatus.PENDING)
        .expiredAt(LocalDateTime.now().plusMinutes(QR_TTL_MINUTES))
        .build();
  }

  // ─── Step 2: Sync status (called by polling + webhook) ──
  @Override
  public void syncStatus(Payment payment) {
    log.debug("[Bakong] Syncing status for payment #{}", payment.getId());

    // Check expiry first — avoids unnecessary API call
    if (payment.getExpiredAt() != null && LocalDateTime.now().isAfter(payment.getExpiredAt())) {
      log.info("[Bakong] Payment #{} expired", payment.getId());
      payment.setStatus(PaymentStatus.FAILED);
      return;
    }

    Map<String, Object> response = checkTransactionByMd5(payment.getGatewayReference());
    if (response == null) return;

    Integer responseCode = (Integer) response.get("responseCode");
    Integer errorCode = (Integer) response.get("errorCode");

    if (responseCode != null && responseCode == 0) {
      // Transaction found & successful
      payment.setStatus(PaymentStatus.PAID);
      payment.setPaidAt(LocalDateTime.now());

      @SuppressWarnings("unchecked")
      Map<String, Object> data = (Map<String, Object>) response.get("data");
      if (data != null) {
        payment.setGatewayTransactionId((String) data.get("hash"));
      }
      log.info("[Bakong] Payment #{} PAID", payment.getId());

    } else if (errorCode != null && errorCode == 3) {
      // Error code 3 = Transaction failed
      payment.setStatus(PaymentStatus.FAILED);
      log.info("[Bakong] Payment #{} FAILED (errorCode=3)", payment.getId());

    } else {
      // errorCode 1 = not found yet — still PENDING
      log.debug("[Bakong] Payment #{} still PENDING (errorCode={})", payment.getId(), errorCode);
    }
  }

  // ─── Private helpers ──────────────────────────────────

  /**
   * Builds a KHQR (EMV QR) string for the order.
   *
   * <p>IMPORTANT: Replace this stub with your actual implementation. Options: - Use NBC's khqr-sdk
   * (npm / Java library) - Call your acquiring bank's QR generation API - Use Wing / ABA / ACLEDA
   * partner API
   *
   * <p>The format follows EMVCo QR Code Specification for Payment Systems.
   */
  private String buildKhqrString(Order order) {
    // TODO: Replace with real KHQR SDK call, e.g.:
    // return KhqrSdk.generate(merchantId, amount, currency, orderRef);

    // Example static KHQR for testing (NOT for production):
    return String.format(
        "00020101021229190015your_account@bank520459995303840540%s5802KH5909YourShop6010Phnom+Penh62100806#%s6304XXXX",
        order.getTotalAmount().toPlainString(), order.getOrderNumber());
  }

  private String computeMd5(String input) {
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : hash) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to compute MD5", e);
    }
  }

  @SuppressWarnings("unchecked")
  private String generateDeeplink(String khqrString, String callbackUrl) {
    String url = bakongBaseUrl + "/v1/generate_deeplink_by_qr";

    Map<String, Object> sourceInfo =
        Map.of(
            "appIconUrl", appIconUrl,
            "appName", appName,
            "appDeepLinkCallback", callbackUrl);
    Map<String, Object> body = Map.of("qr", khqrString, "sourceInfo", sourceInfo);

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      ResponseEntity<Map> response =
          restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

      Map<String, Object> resp = response.getBody();
      if (resp != null && Integer.valueOf(0).equals(resp.get("responseCode"))) {
        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        return data != null ? (String) data.get("shortLink") : khqrString;
      }
    } catch (Exception e) {
      log.warn("[Bakong] Failed to generate deeplink, falling back to raw QR: {}", e.getMessage());
    }

    // Fallback: return raw KHQR string so client can render it as QR image
    return khqrString;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> checkTransactionByMd5(String md5) {
    String url = bakongBaseUrl + "/v1/check_transaction_by_md5";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(bakongToken);

    try {
      ResponseEntity<Map> response =
          restTemplate.exchange(
              url, HttpMethod.POST, new HttpEntity<>(Map.of("md5", md5), headers), Map.class);
      return response.getBody();
    } catch (Exception e) {
      log.error("[Bakong] Error checking transaction: {}", e.getMessage());
      return null;
    }
  }
}
