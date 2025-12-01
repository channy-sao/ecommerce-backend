package ecommerce_app.modules.category.model.entity;

import ecommerce_app.infrastructure.model.entity.AuditingEntity;
import ecommerce_app.modules.product.model.entity.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Table(name = "categories", indexes = @Index(columnList = "name", name = "name_index"))
@Getter
@Setter
@Entity
public class Category extends AuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100, name = "name")
  private String name;

  @Column(length = 100, name = "description")
  private String description;

  @OneToMany(
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY,
      mappedBy = "category",
      targetEntity = Product.class)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private List<Product> products;
}
