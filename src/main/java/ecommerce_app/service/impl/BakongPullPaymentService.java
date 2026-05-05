package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.dto.request.PullPaymentRequest;
import ecommerce_app.dto.response.PullPaymentResponse;
import ecommerce_app.dto.response.PullPaymentStatusResponse;
import ecommerce_app.entity.KHQRPayment;
import ecommerce_app.entity.Order;
import ecommerce_app.property.KHQRConfig;
import ecommerce_app.repository.KHQRPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class BakongPullPaymentService {

  private final KHQRConfig khqrConfig;
  private final KHQRPaymentRepository khqrPaymentRepository;
  private final RestTemplate restTemplate;

  /** Initiate pull payment - request money from customer's Bakong account */
  @Transactional
  public PullPaymentResponse initiatePullPayment(PullPaymentRequest request) {
    // Validate phone number format
    validatePhoneNumber(request.getCustomerPhone());

    // Create payment record
    KHQRPayment payment =
        KHQRPayment.builder()
            .customerId(formatPhoneNumber(request.getCustomerPhone()))
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .status(PaymentStatus.PENDING)
            .expiresAt(Instant.now().plus(khqrConfig.getDefaultExpiryMinutes(), ChronoUnit.MINUTES))
            .build();

    // Generate MD5 hash for security
    String md5Hash = generateTransactionHash(request);
    payment.setQrMd5(md5Hash);

    payment = khqrPaymentRepository.save(payment);

    // Call Bakong API to initiate pull transaction
    try {
      Map<String, Object> bakongResponse = callBakongPullApi(request, payment.getId());

      if (bakongResponse != null && "SUCCESS".equals(bakongResponse.get("responseCode"))) {
        payment.setBakongTransactionId((String) bakongResponse.get("transactionId"));
        payment = khqrPaymentRepository.save(payment);

        return buildPullPaymentResponse(payment, request, bakongResponse);
      } else {
        payment.setStatus(PaymentStatus.FAILED);
        payment = khqrPaymentRepository.save(payment);
        throw new RuntimeException(
            "Bakong pull payment initiation failed: "
                + (bakongResponse != null
                    ? bakongResponse.get("responseMessage")
                    : "Unknown error"));
      }
    } catch (Exception e) {
      log.error("Failed to initiate pull payment", e);
      payment.setStatus(PaymentStatus.FAILED);
      khqrPaymentRepository.save(payment);
      throw new RuntimeException("Failed to initiate payment request", e);
    }
  }

  /** Check status of pull payment */
  @Transactional
  public PullPaymentStatusResponse checkPullPaymentStatus(String paymentId) {
    KHQRPayment payment =
        khqrPaymentRepository
            .findById(Long.valueOf(paymentId))
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

    // Check if payment has expired
    if (payment.getStatus() == PaymentStatus.PENDING
        && payment.getExpiresAt() != null
        && Instant.now().isAfter(payment.getExpiresAt())) {
      payment.setStatus(PaymentStatus.FAILED);
      payment = khqrPaymentRepository.save(payment);

      return PullPaymentStatusResponse.builder()
          .paymentId(payment.getId())
          .status("EXPIRED")
          .build();
    }

    // If we have a Bakong transaction ID, check with Bakong API
    if (payment.getBakongTransactionId() != null && payment.getStatus() == PaymentStatus.PENDING) {
      try {
        Map<String, Object> statusResponse = callBakongStatusApi(payment.getBakongTransactionId());

        if (statusResponse != null) {
          String bakongStatus = (String) statusResponse.get("status");

          switch (bakongStatus.toUpperCase()) {
            case "APPROVED", "SUCCESS":
              payment.setStatus(PaymentStatus.COMPLETED);
              payment.setPaidAt(Instant.now());
              payment = khqrPaymentRepository.save(payment);
              break;
            case "DECLINED", "REJECTED", "EXPIRED":
              payment.setStatus(PaymentStatus.FAILED);
              payment = khqrPaymentRepository.save(payment);
              break;
          }

          return mapBakongStatusResponse(payment, statusResponse);
        }
      } catch (Exception e) {
        log.error("Error checking Bakong payment status", e);
      }
    }

    // Return current status from our database
    return PullPaymentStatusResponse.builder()
        .paymentId(payment.getId())
        .transactionId(payment.getBakongTransactionId())
        .status(mapStatusToString(payment.getStatus()))
        .amount(payment.getAmount().toString())
        .currency(payment.getCurrency())
        .build();
  }

  /** Cancel pull payment request */
  @Transactional
  public void cancelPullPayment(String paymentId) {
    KHQRPayment payment =
        khqrPaymentRepository
            .findById(Long.valueOf(paymentId))
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

    if (payment.getStatus() != PaymentStatus.PENDING) {
      throw new RuntimeException("Can only cancel pending payments");
    }

    // Cancel with Bakong API if transaction exists
    if (payment.getBakongTransactionId() != null) {
      try {
        callBakongCancelApi(payment.getBakongTransactionId());
      } catch (Exception e) {
        log.error("Error canceling Bakong transaction", e);
      }
    }

    payment.setStatus(PaymentStatus.FAILED);
    khqrPaymentRepository.save(payment);
  }

  /** Call Bakong API to initiate pull transaction */
  private Map<String, Object> callBakongPullApi(PullPaymentRequest request, String merchantTxnId) {
    String url = khqrConfig.getApiBaseUrl() + "/v1/transactions/pull";

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("fromAccountId", formatPhoneNumber(request.getCustomerPhone()));
    requestBody.put("toAccountId", khqrConfig.getBakongAccountId());
    requestBody.put("amount", request.getAmount().doubleValue());
    requestBody.put("currency", request.getCurrency());
    requestBody.put("merchantId", khqrConfig.getMerchantId());
    requestBody.put("merchantTxnId", merchantTxnId);
    requestBody.put("description", request.getDescription());
    requestBody.put("orderNumber", request.getOrderNumber());

    // Generate signature
    String signature = generateApiSignature(requestBody);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-API-Key", khqrConfig.getApiKey());
    headers.set("X-Signature", signature);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
      return response.getBody();
    } catch (Exception e) {
      log.error("Bakong API call failed", e);
      return null;
    }
  }

  /** Check transaction status with Bakong API */
  private Map<String, Object> callBakongStatusApi(String transactionId) {
    String url = khqrConfig.getApiBaseUrl() + "/v1/transactions/" + transactionId + "/status";

    HttpHeaders headers = new HttpHeaders();
    headers.set("X-API-Key", khqrConfig.getApiKey());

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
      return response.getBody();
    } catch (Exception e) {
      log.error("Bakong status API call failed", e);
      return null;
    }
  }

  /** Cancel transaction with Bakong API */
  private void callBakongCancelApi(String transactionId) {
    String url = khqrConfig.getApiBaseUrl() + "/v1/transactions/" + transactionId + "/cancel";

    HttpHeaders headers = new HttpHeaders();
    headers.set("X-API-Key", khqrConfig.getApiKey());

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
  }

  private void validatePhoneNumber(String phone) {
    if (phone == null || !phone.matches("^(0|\\+855)[1-9][0-9]{7,8}$")) {
      throw new IllegalArgumentException(
          "Invalid phone number format. Expected: 0XXXXXXXX or +855XXXXXXXX");
    }
  }

  private String formatPhoneNumber(String phone) {
    // Convert to Bakong format: 855XXXXXXXX
    if (phone.startsWith("0")) {
      return "855" + phone.substring(1);
    } else if (phone.startsWith("+855")) {
      return phone.substring(1);
    }
    return phone;
  }

  private String generateTransactionHash(PullPaymentRequest request) {
    String data =
        request.getCustomerPhone()
            + request.getAmount()
            + request.getCurrency()
            + System.currentTimeMillis();
    return calculateMd5(data);
  }

  private String generateApiSignature(Map<String, Object> requestBody) {
    String data = requestBody.toString() + khqrConfig.getApiSecret();
    return calculateMd5(data);
  }

  private String calculateMd5(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(input.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not available", e);
    }
  }

  private PullPaymentResponse buildPullPaymentResponse(
      KHQRPayment payment, PullPaymentRequest request, Map<String, Object> bakongResponse) {

    return PullPaymentResponse.builder()
        .paymentId(payment.getId())
        .transactionId(payment.getBakongTransactionId())
        .customerPhone(request.getCustomerPhone())
        .amount(request.getAmount())
        .currency(request.getCurrency())
        .status(PaymentStatus.PENDING)
        .statusMessage("Payment request sent to customer. Awaiting approval.")
        .createdAt(Instant.from(payment.getCreatedAt()))
        .expiresAt(payment.getExpiresAt())
        .orderNumber(request.getOrderNumber())
        .merchantName(khqrConfig.getMerchantName())
        .storeName(khqrConfig.getMerchantName())
        .build();
  }

  private PullPaymentStatusResponse mapBakongStatusResponse(
      KHQRPayment payment, Map<String, Object> bakongResponse) {

    return PullPaymentStatusResponse.builder()
        .paymentId(payment.getId())
        .transactionId(payment.getBakongTransactionId())
        .status((String) bakongResponse.get("status"))
        .customerPhone(payment.getCustomerId())
        .customerName((String) bakongResponse.get("customerName"))
        .amount(payment.getAmount().toString())
        .currency(payment.getCurrency())
        .approvalTimestamp((String) bakongResponse.get("approvalTimestamp"))
        .rejectionReason((String) bakongResponse.get("rejectionReason"))
        .receiptUrl((String) bakongResponse.get("receiptUrl"))
        .build();
  }

  private String mapStatusToString(PaymentStatus status) {
    return switch (status) {
      case PENDING -> "PENDING";
      case COMPLETED -> "APPROVED";
      case FAILED -> "DECLINED";
      default -> "UNKNOWN";
    };
  }
}
