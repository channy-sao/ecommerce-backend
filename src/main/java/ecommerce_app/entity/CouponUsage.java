// CouponUsage.java
package ecommerce_app.entity;

import ecommerce_app.entity.base.TimeAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "coupon_usages",
    indexes = {
      @Index(name = "idx_coupon_usage_coupon_id", columnList = "coupon_id"),
      @Index(name = "idx_coupon_usage_user_id", columnList = "user_id"),
      @Index(name = "idx_coupon_usage_order_id", columnList = "order_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUsage extends TimeAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "coupon_id", nullable = false)
  private Coupon coupon;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal discountAmount; // actual amount discounted

  @Column(name = "used_at", nullable = false)
  @Builder.Default
  private LocalDateTime usedAt = LocalDateTime.now();
}
