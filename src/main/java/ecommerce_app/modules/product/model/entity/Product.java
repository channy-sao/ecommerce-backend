package ecommerce_app.modules.product.model.entity;

import ecommerce_app.infrastructure.model.entity.AuditingEntity;
import ecommerce_app.modules.cart.model.entity.CartItem;
import ecommerce_app.modules.category.model.entity.Category;
import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.modules.stock.model.entity.ProductImport;
import ecommerce_app.modules.stock.model.entity.Stock;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Table(
    name = "products",
    indexes = {
      @Index(columnList = "name", name = "product_name_index"),
      @Index(columnList = "uuid", name = "product_uuid_index")
    })
@Getter
@Setter
@Entity
public class Product extends AuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100, name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(nullable = false, name = "price")
  private BigDecimal price;

  @Column(nullable = false, name = "image", length = 500)
  private String image;

  @Column(nullable = false, name = "is_feature")
  private Boolean isFeature;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false,
      targetEntity = Category.class,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Category category;

  @OneToMany(
      cascade = {CascadeType.MERGE, CascadeType.PERSIST},
      fetch = FetchType.LAZY,
      targetEntity = CartItem.class,
      mappedBy = "product")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private List<CartItem> cartItems;

  @OneToMany(
          cascade = {CascadeType.MERGE, CascadeType.PERSIST},
          fetch = FetchType.LAZY,
          targetEntity = OrderItem.class,
          mappedBy = "product")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private List<OrderItem> orderItems;

  @Column(nullable = false, unique = true, name = "uuid")
  private UUID uuid = UUID.randomUUID();

  @OneToMany(
          cascade = {CascadeType.MERGE, CascadeType.PERSIST},
          fetch = FetchType.LAZY,
          targetEntity = ProductImport.class,
          mappedBy = "product")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private List<ProductImport> productImports;

  @OneToMany(
          cascade = {CascadeType.MERGE, CascadeType.PERSIST},
          fetch = FetchType.LAZY,
          targetEntity = Stock.class,
          mappedBy = "product")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private List<Stock> stocks;

}
