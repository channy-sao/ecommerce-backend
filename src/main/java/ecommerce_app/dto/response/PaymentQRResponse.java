package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQRResponse {

  private String paymentId;
  private Long orderId;
  private String orderNumber;
  private BigDecimal amount;
  private String currency;

  // QR related fields
  private String qrString;
  private String qrImageBase64; // Ready to display in img tag
  private String deepLink;

  // Status and timing
  private PaymentStatus status;
  private Instant expiresAt;
  private long remainingSeconds;
  private LocalDateTime createdAt;

  // Merchant info
  private String merchantName;

  // Payment instructions
  private String instructions;
  private String[] supportedApps = {
    "Bakong App", "ABA Mobile", "ACLEDA Mobile", "Wing Bank", "Other Bakong Partners"
  };
}
