package ecommerce_app.entity;

import ecommerce_app.entity.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Table(name = "product_specs")
@Getter
@Setter
@Entity
public class ProductSpec extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Product product;

  @Column(nullable = false, name = "spec_text", length = 255)
  private String specText;

  @Column(nullable = false, name = "sort_order")
  private Integer sortOrder = 0;
}
