// Coupon.java
package ecommerce_app.entity;

import ecommerce_app.constant.enums.CouponDiscountType;
import ecommerce_app.entity.base.TimeAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "coupons",
    indexes = {
      @Index(name = "idx_coupon_code", columnList = "code"),
      @Index(name = "idx_coupon_active", columnList = "is_active"),
      @Index(name = "idx_coupon_start_date", columnList = "start_date"),
      @Index(name = "idx_coupon_end_date", columnList = "end_date")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends TimeAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String code; // "SAVE10", "WELCOME50"

  @Column(length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type", nullable = false, length = 20)
  private CouponDiscountType discountType;

  @Column(name = "discount_value", precision = 10, scale = 2)
  private BigDecimal discountValue; // 10.00 = 10% or $10

  @Column(name = "min_order_amount", precision = 10, scale = 2)
  private BigDecimal minOrderAmount; // minimum cart total to apply

  @Column(name = "max_discount", precision = 10, scale = 2)
  private BigDecimal maxDiscount; // cap: max $20 off even on large orders

  @Column(name = "usage_limit")
  private Integer usageLimit; // total uses allowed (null = unlimited)

  @Column(name = "usage_per_user", nullable = false)
  @Builder.Default
  private Integer usagePerUser = 1; // how many times one user can use

  @Column(name = "used_count", nullable = false)
  @Builder.Default
  private Integer usedCount = 0; // total times used so far

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<CouponUsage> usages = new ArrayList<>();

  // ── Transient helpers ─────────────────────────────────────────────────────

  @Transient
  public boolean isCurrentlyValid() {
    LocalDateTime now = LocalDateTime.now();
    if (startDate != null && now.isBefore(startDate)) return false;
    if (endDate != null && now.isAfter(endDate)) return false;
    return true;
  }

  @Transient
  public boolean hasReachedUsageLimit() {
    return usageLimit != null && usedCount >= usageLimit;
  }
}
