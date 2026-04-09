package ecommerce_app.entity;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Stores every payment attempt for an order. One order can have multiple attempts (e.g. failed then
 * succeeded).
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // ── Order relationship ────────────────────────────────
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  // ── Gateway info ──────────────────────────────────────
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentGateway gateway; // BAKONG | STRIPE

  /** Gateway-specific transaction / charge ID returned after creation */
  @Column(name = "gateway_transaction_id")
  private String gatewayTransactionId;

  /** For Bakong: the MD5 hash of the KHQR string (used for status polling) */
  @Column(name = "gateway_reference")
  private String gatewayReference;

  /** For Bakong: deeplink URL sent to client; for Stripe: payment-intent client secret */
  @Column(name = "payment_url", length = 1024)
  private String paymentUrl;

  // ── Amounts ───────────────────────────────────────────
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "cash_received", precision = 12, scale = 2)
  private BigDecimal cashReceived;

  @Column(name = "change_amount", precision = 12, scale = 2)
  private BigDecimal changeAmount;

  @Column(nullable = false, length = 3)
  private String currency; // "USD" | "KHR"

  // ── Status ────────────────────────────────────────────
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status; // PENDING | PAID | FAILED | REFUNDED

  // ── Timestamps ───────────────────────────────────────
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "paid_at")
  private LocalDateTime paidAt;

  @Column(name = "expired_at")
  private LocalDateTime expiredAt;

  /** Raw webhook / callback payload stored for auditing */
  @Column(name = "raw_callback", columnDefinition = "TEXT")
  private String rawCallback;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (status == null) status = PaymentStatus.PENDING;
  }
}
