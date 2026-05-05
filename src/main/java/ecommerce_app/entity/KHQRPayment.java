package ecommerce_app.entity;

import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.entity.base.TimeAuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "khqr_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KHQRPayment extends TimeAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @OneToOne(mappedBy = "khqrPayment", optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(nullable = false)
  private String customerId;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String qrString;

  @Column(nullable = false, length = 32)
  private String qrMd5;

  @Column private String deepLink;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column private Instant expiresAt;

  @Column private Instant paidAt;

  @Column private String bakongTransactionId;

  @Column private String callbackMd5;

  @Column(columnDefinition = "TEXT")
  private String callbackRawPayload;
}
