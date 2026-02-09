package ecommerce_app.modules.product.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.infrastructure.model.entity.SoftDeletableEntity;
import ecommerce_app.modules.cart.model.entity.CartItem;
import ecommerce_app.modules.category.model.entity.Category;
import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.modules.promotion.model.entity.Promotion;
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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Table(
    name = "products",
    indexes = {
      @Index(columnList = "name", name = "product_name_index"),
      @Index(columnList = "uuid", name = "product_uuid_index")
    })
@Getter
@Setter
@Entity
@SQLDelete(sql = "UPDATE products SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Product extends SoftDeletableEntity {
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

  @Column(nullable = false, name = "favorites_count")
  private Integer favoritesCount = 0;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false,
      targetEntity = Category.class,
      cascade = CascadeType.MERGE) // remove persist it error when try fetch category to insert product
  @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private Category category;

  @OneToMany(
      cascade = {CascadeType.MERGE, CascadeType.PERSIST},
      fetch = FetchType.LAZY,
      targetEntity = CartItem.class,
      mappedBy = "product")
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private List<CartItem> cartItems;

  @OneToMany(
      cascade = {CascadeType.MERGE, CascadeType.PERSIST},
      fetch = FetchType.LAZY,
      targetEntity = OrderItem.class,
      mappedBy = "product")
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private List<OrderItem> orderItems;

  @Column(nullable = false, unique = true, name = "uuid")
  private UUID uuid = UUID.randomUUID();

  @OneToMany(
      cascade = {CascadeType.MERGE, CascadeType.PERSIST},
      fetch = FetchType.LAZY,
      targetEntity = ProductImport.class,
      mappedBy = "product")
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private List<ProductImport> productImports;

  @OneToOne(
      cascade = {CascadeType.MERGE, CascadeType.PERSIST},
      targetEntity = Stock.class,
      mappedBy = "product")
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private Stock stock;

  // Add this field to the Product class
  @ManyToMany(mappedBy = "products")
  @JsonIgnore
  private List<Promotion> promotions;

  @Transient
  public int getStockQuantity() {
    if (stock == null) {
      return 0;
    }
    return stock.getQuantity();
  }
}
