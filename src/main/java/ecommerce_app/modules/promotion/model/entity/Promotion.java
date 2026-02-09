package ecommerce_app.modules.promotion.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.infrastructure.model.entity.UserAuditableEntity;
import ecommerce_app.modules.product.model.entity.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "promotions",
    indexes = {
      @Index(name = "idx_promotion_code", columnList = "code", unique = true),
      @Index(name = "idx_promotion_active", columnList = "active"),
      @Index(name = "idx_promotion_start_at", columnList = "start_at"),
      @Index(name = "idx_promotion_end_at", columnList = "end_at"),
      @Index(name = "idx_promotion_date_range", columnList = "start_at, end_at"),
      @Index(name = "idx_promotion_discount_type", columnList = "discount_type"),
      @Index(name = "idx_promotion_active_dates", columnList = "active, start_at, end_at"),
      @Index(name = "idx_promotion_created_at", columnList = "created_at"),
      @Index(name = "idx_promotion_name", columnList = "name")
    })
@Getter
@Setter
public class Promotion extends UserAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String code; // optional coupon code

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PromotionType discountType;

  @Column(name = "discount_value", precision = 10, scale = 2)
  private BigDecimal discountValue; // % or fixed

  @Column(name = "buy_quantity")
  private Integer buyQuantity;

  @Column(name = "get_quantity")
  private Integer getQuantity;

  @Column(name = "active", nullable = false)
  private Boolean active = true;

  @Column(name = "start_at")
  private LocalDateTime startAt;

  @Column(name = "end_at")
  private LocalDateTime endAt;

  @Column(name = "max_usage")
  private Integer maxUsage;

  @Column(name = "max_usage_per_user")
  private Integer maxUsagePerUser; // Maximum times per user

  @Column(name = "min_purchase_amount", precision = 10, scale = 2)
  private BigDecimal minPurchaseAmount; // Minimum order amount required

  @ManyToMany
  @JoinTable(
      name = "promotion_products",
      joinColumns = @JoinColumn(name = "promotion_id"),
      inverseJoinColumns = @JoinColumn(name = "product_id"))
  private List<Product> products;

  @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<PromotionUsage> usages = new ArrayList<>();

  /** Check if promotion is currently active and valid */
  public boolean isCurrentlyValid() {
    if (Boolean.FALSE.equals(active)) {
      return false;
    }

    LocalDateTime now = LocalDateTime.now();

    if (startAt != null && now.isBefore(startAt)) {
      return false;
    }

    return endAt == null || !now.isAfter(endAt);
  }

  /** Check if promotion has reached max usage */
  public boolean hasReachedMaxUsage() {
    if (maxUsage == null) {
      return false;
    }
    return usages != null && usages.size() >= maxUsage;
  }

  /** Get remaining usage count */
  public Integer getRemainingUsage() {
    if (maxUsage == null) {
      return null; // unlimited
    }
    int currentUsage = usages != null ? usages.size() : 0;
    return Math.max(0, maxUsage - currentUsage);
  }

  @PrePersist
  @PreUpdate
  public void validateDates() {
    if (startAt != null && endAt != null && startAt.isAfter(endAt)) {
      throw new IllegalStateException("Start date cannot be after end date");
    }
  }
}
