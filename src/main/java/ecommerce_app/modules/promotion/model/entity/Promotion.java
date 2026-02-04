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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "promotions")
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

  private BigDecimal discountValue; // % or fixed

  private Integer buyQuantity;
  private Integer getQuantity;

  private Boolean active = true;

  private LocalDateTime startAt;
  private LocalDateTime endAt;

  private Integer maxUsage;

  @ManyToMany
  @JoinTable(
      name = "promotion_products",
      joinColumns = @JoinColumn(name = "promotion_id"),
      inverseJoinColumns = @JoinColumn(name = "product_id"))
  private List<Product> products;

  @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<PromotionUsage> usages = new ArrayList<>();
}
