package ecommerce_app.modules.product.model.entity;

import ecommerce_app.infrastructure.model.entity.TimeAuditableEntity;
import ecommerce_app.modules.user.model.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "favorites",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "product_id"})},
    indexes = {
      @Index(name = "idx_favorite_user", columnList = "user_id"),
      @Index(name = "idx_favorite_product", columnList = "product_id")
    })
@Getter
@Setter
public class Favorite extends TimeAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;
}
