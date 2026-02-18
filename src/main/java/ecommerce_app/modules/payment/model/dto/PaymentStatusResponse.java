package ecommerce_app.modules.payment.model.dto;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ─────────────────────────────────────────────────────
// Response: payment status (used by polling + webhook)
// ─────────────────────────────────────────────────────
@Data
@Builder
public class PaymentStatusResponse {

  private Long paymentId;
  private Long orderId;
  private PaymentGateway gateway;
  private PaymentStatus status;
  private BigDecimal amount;
  private String currency;
  private LocalDateTime paidAt;
  private String message;
}
