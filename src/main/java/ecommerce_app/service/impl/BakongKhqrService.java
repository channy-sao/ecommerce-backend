package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.dto.request.BakongCallbackPayload;
import ecommerce_app.dto.response.PaymentResponse;
import ecommerce_app.dto.response.PaymentStatusResponseV2;
import ecommerce_app.entity.KHQRPayment;
import ecommerce_app.entity.Order;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.property.KHQRConfig;
import ecommerce_app.repository.KHQRPaymentRepository;
import ecommerce_app.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import kh.gov.nbc.bakong_khqr.model.MerchantInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BakongKhqrService {
  private final KHQRConfig khqrConfig;
  private final KHQRPaymentRepository khqrPaymentRepository;
  private final OrderRepository orderRepository;

  @Transactional
  public PaymentResponse createPayment(Long orderId) {
    // check order is exist
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

    // Check if payment already exists
    Optional<KHQRPayment> existingPayment = khqrPaymentRepository.findByOrderId(order.getId());
    if (existingPayment.isPresent()) {
      return PaymentResponse.from(existingPayment.get());
    }

    // Create merchant info from config
    MerchantInfo merchantInfo = buildMerchantInfo(order);

    // Generate QR
    KHQRResponse<KHQRData> response = BakongKHQR.generateMerchant(merchantInfo);

    if (response.getKHQRStatus().getCode() != 0) {
      log.error("KHQR generation failed: {}", response.getKHQRStatus().getMessage());
      throw new RuntimeException(
          "Failed to generate KHQR: " + response.getKHQRStatus().getMessage());
    }

    KHQRData qrData = response.getData();

    // Create and save payment record
    KHQRPayment payment =
        KHQRPayment.builder()
            .order(order)
            .customerId(order.getUser().getId().toString())
            .amount(order.getTotalAmount())
            .currency(khqrConfig.getCurrency().name())
            .qrString(qrData.getQr())
            .qrMd5(calculateMd5(qrData.getQr()))
            .deepLink(generateDeepLink(qrData.getQr()))
            .status(PaymentStatus.PENDING)
            .expiresAt(Instant.now().plus(khqrConfig.getDefaultExpiryMinutes(), ChronoUnit.MINUTES))
            .build();

    payment = khqrPaymentRepository.save(payment);

    log.info(
        "KHQR payment created: paymentId={}, orderId={}, amount={}",
        payment.getId(),
        order.getId(),
        payment.getAmount());

    return PaymentResponse.from(payment);
  }

  private MerchantInfo buildMerchantInfo(Order order) {
    MerchantInfo merchantInfo = new MerchantInfo();

    merchantInfo.setBakongAccountId(khqrConfig.getBakongAccountId());
    merchantInfo.setMerchantId(khqrConfig.getMerchantId());
    merchantInfo.setAcquiringBank(khqrConfig.getAcquiringBank());
    merchantInfo.setMerchantName(khqrConfig.getMerchantName());
    merchantInfo.setMerchantCity(khqrConfig.getMerchantCity());

    if (khqrConfig.getMerchantCategoryCode() != null) {
      merchantInfo.setMerchantCategoryCode(khqrConfig.getMerchantCategoryCode());
    }
    if (khqrConfig.getMerchantNameKhmer() != null) {
      merchantInfo.setMerchantNameAlternateLanguage(khqrConfig.getMerchantNameKhmer());
    }
    if (khqrConfig.getMerchantCityKhmer() != null) {
      merchantInfo.setMerchantName(khqrConfig.getMerchantCityKhmer());
    }

    merchantInfo.setCurrency(KHQRCurrency.valueOf(khqrConfig.getCurrency().name()));
    merchantInfo.setAmount(order.getTotalAmount().doubleValue());
    merchantInfo.setBillNumber(order.getOrderNumber());
    merchantInfo.setStoreLabel("Online Store");
    merchantInfo.setTerminalLabel("WEB");
    merchantInfo.setPurposeOfTransaction("Order Payment");

    // Set expiration
    merchantInfo.setExpirationTimestamp(
        System.currentTimeMillis() + (khqrConfig.getDefaultExpiryMinutes() * 60 * 1000L));

    return merchantInfo;
  }

  @Transactional
  public PaymentStatusResponseV2 handleCallback(BakongCallbackPayload payload) {
    // Validate callback
    validateCallback(payload);

    // Find payment by transaction ID
    KHQRPayment payment =
        khqrPaymentRepository
            .findByBakongTransactionId(payload.getTransactionId())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Payment not found for transaction: " + payload.getTransactionId()));

    // Update payment status
    PaymentStatus newStatus = mapBakongStatus(payload.getStatus());
    payment.setStatus(newStatus);

    if (newStatus == PaymentStatus.COMPLETED) {
      payment.setPaidAt(Instant.now());
      payment.setBakongTransactionId(payload.getTransactionId());
    }

    payment.setCallbackRawPayload(payload.toString());
    payment.setCallbackMd5(payload.getMd5());

    payment = khqrPaymentRepository.save(payment);

    // Update order status if payment completed
    if (newStatus == PaymentStatus.COMPLETED) {
      updateOrderStatus(payment.getOrder());
    }

    log.info("Payment callback processed: paymentId={}, status={}", payment.getId(), newStatus);

    return PaymentStatusResponseV2.from(payment);
  }

  @Transactional(readOnly = true)
  public PaymentStatusResponseV2 checkPaymentStatus(String paymentId) {
    KHQRPayment payment =
        khqrPaymentRepository
            .findById(Long.valueOf(paymentId))
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

    // Check if payment is expired
    if (payment.getStatus() == PaymentStatus.PENDING
        && payment.getExpiresAt() != null
        && Instant.now().isAfter(payment.getExpiresAt())) {
      payment.setStatus(PaymentStatus.FAILED);
      payment = khqrPaymentRepository.save(payment);
    }

    return PaymentStatusResponseV2.from(payment);
  }

  private void validateCallback(BakongCallbackPayload payload) {
    // Validate MD5 hash
    String calculatedMd5 = calculateCallbackMd5(payload);
    if (!calculatedMd5.equals(payload.getMd5())) {
      log.error("Invalid callback MD5: expected={}, received={}", calculatedMd5, payload.getMd5());
      throw new RuntimeException("Invalid callback signature");
    }

    // Validate timestamp (optional - within 5 minutes)
    // Add timestamp validation if needed
  }

  private String calculateCallbackMd5(BakongCallbackPayload payload) {
    // Implement according to Bakong's specification
    // This is a placeholder - actual implementation depends on their docs
    String raw =
        payload.getTransactionId()
            + payload.getStatus()
            + payload.getAmount()
            + payload.getCurrency()
            + payload.getToAccountId()
            + khqrConfig.getMerchantSecret();

    return calculateMd5(raw);
  }

  private String generateDeepLink(String qrString) {
    if (khqrConfig.getDeepLinkApiUrl() != null) {
      return khqrConfig.getDeepLinkApiUrl() + qrString;
    }
    return null;
  }

  private PaymentStatus mapBakongStatus(String bakongStatus) {
    // Map Bakong status codes to your PaymentStatus enum
    return switch (bakongStatus.toUpperCase()) {
      case "SUCCESS", "COMPLETED" -> PaymentStatus.COMPLETED;
      case "PENDING" -> PaymentStatus.PENDING;
      default -> PaymentStatus.FAILED;
    };
  }

  private void updateOrderStatus(Order order) {
    // Update order status logic here
    // order.setStatus(OrderStatus.PAID);
    // orderRepository.save(order);
    log.info("Updating order status for orderId={}", order.getId());
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
}
