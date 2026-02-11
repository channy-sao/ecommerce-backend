package ecommerce_app.modules.category.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.infrastructure.model.entity.TimeAuditableEntity;
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

@Table(
    name = "categories",
    indexes = {
      @Index(columnList = "name", name = "name_index"),
      @Index(name = "idx_category_created", columnList = "created_at"),
      @Index(name = "idx_category_updated", columnList = "updated_at"),
      @Index(name = "idx_category_display_order", columnList = "display_order")
    })
@Getter
@Setter
@Entity
public class Category extends TimeAuditableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100, name = "name")
  private String name;

  @Column(name = "icon", length = 10)
  private String icon;

  @Column(length = 500, name = "description")
  private String description;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder = 0;

  @OneToMany(
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY,
      mappedBy = "category",
      targetEntity = Product.class)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private List<Product> products;
}
