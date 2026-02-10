package ecommerce_app.modules.review.model.entity;

import ecommerce_app.infrastructure.model.entity.UserAuditableEntity;
import ecommerce_app.modules.product.model.entity.Product;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(
    name = "reviews",
    indexes = {
      @Index(name = "idx_review_product", columnList = "product_id"),
      @Index(name = "idx_review_approved", columnList = "approved"),
      @Index(name = "idx_review_user", columnList = "user_id")
    })
@Getter
@Setter
public class Review extends UserAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // product relation is OK
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id")
  private Product product;

  // store ONLY ID
  @Column(name = "user_id", nullable = false)
  private Long userId;

  // snapshot username (best practice)
  @Column(name = "username", nullable = false, length = 100)
  private String username;

  @Column(nullable = false)
  private Integer rating; // 1–5

  @Column(length = 1000)
  private String comment;

  @Column(nullable = false)
  private Boolean approved = false;
}
