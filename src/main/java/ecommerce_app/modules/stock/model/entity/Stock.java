package ecommerce_app.modules.stock.model.entity;

import ecommerce_app.infrastructure.model.entity.AuditingEntity;
import ecommerce_app.modules.product.model.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "stocks")
public class Stock extends AuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(
      optional = false,
      targetEntity = Product.class,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST},
      fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false, unique = true)
  private Product product;

  @Column(nullable = false, name = "quantity")
  private int quantity;
}
