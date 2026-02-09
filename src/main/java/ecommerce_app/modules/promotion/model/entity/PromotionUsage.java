package ecommerce_app.modules.promotion.model.entity;

import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.user.model.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "promotion_usages",
    indexes = {
      @Index(name = "idx_promo_usage_promotion_id", columnList = "promotion_id"),
      @Index(name = "idx_promo_usage_user_id", columnList = "user_id"),
      @Index(name = "idx_promo_usage_order_id", columnList = "order_id", unique = true),
      @Index(name = "idx_promo_usage_promo_user", columnList = "promotion_id, user_id"),
      @Index(name = "idx_promo_usage_used_at", columnList = "used_at"),
      @Index(name = "idx_promo_usage_promo_date", columnList = "promotion_id, used_at"),
      @Index(name = "idx_promo_usage_user_date", columnList = "user_id, used_at"),
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionUsage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "promotion_id", nullable = false)
  private Promotion promotion;

  @Column(name = "discount_amount", nullable = false)
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, name = "used_at")
  private LocalDateTime usedAt = LocalDateTime.now();

  // Optional: Track original order total for analytics
  @Column(name = "order_total", precision = 10, scale = 2)
  private BigDecimal orderTotal;

  @PrePersist
  public void prePersist() {
    if (usedAt == null) {
      usedAt = LocalDateTime.now();
    }
    if (discountAmount == null) {
      discountAmount = BigDecimal.ZERO;
    }
  }
}
