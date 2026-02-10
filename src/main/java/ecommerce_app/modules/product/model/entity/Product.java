package ecommerce_app.modules.product.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.infrastructure.model.entity.SoftDeletableEntity;
import ecommerce_app.modules.cart.model.entity.CartItem;
import ecommerce_app.modules.category.model.entity.Category;
import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import ecommerce_app.modules.review.model.entity.Review;
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
import java.math.RoundingMode;
import java.util.Comparator;
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
      cascade =
          CascadeType.MERGE) // remove persist it error when try fetch category to insert product
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

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  private List<Review> reviews;

  @Transient
  public int getStockQuantity() {
    if (stock == null) {
      return 0;
    }
    return stock.getQuantity();
  }

  @Transient
  public String getShortDescription() {
    if (description == null || description.length() <= 100) {
      return description;
    }
    return description.substring(0, 97) + "...";
  }

  @Transient
  public Boolean getInStock() {
    return getStockQuantity() > 0;
  }

  @Transient
  public String getStockStatus() {
    int quantity = getStockQuantity();
    if (quantity <= 0) return "OUT_OF_STOCK";
    if (quantity <= 10) return "LOW_STOCK";
    return "IN_STOCK";
  }

  @Transient
  public BigDecimal getDiscountedPrice() {
    if (promotions != null && !promotions.isEmpty()) {
      Promotion activePromo =
          promotions.stream()
              .filter(Promotion::getActive)
              .filter(Promotion::isCurrentlyValid)
              .max(Comparator.comparing(Promotion::getDiscountValue))
              .orElse(null);

      if (activePromo != null && activePromo.getDiscountValue() != null) {
        if (activePromo.getDiscountType() == PromotionType.PERCENTAGE) {
          BigDecimal discount =
              price
                  .multiply(activePromo.getDiscountValue())
                  .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
          return price.subtract(discount);
        } else if (activePromo.getDiscountType() == PromotionType.FIXED_AMOUNT) {
          return price.subtract(activePromo.getDiscountValue());
        }
      }
    }
    return price;
  }

  @Transient
  public Integer getDiscountPercentage() {
    BigDecimal discounted = getDiscountedPrice();
    if (discounted.compareTo(price) < 0) {
      return price
          .subtract(discounted)
          .divide(price, 2, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf(100))
          .intValue();
    }
    return null;
  }

  @Transient
  public Boolean getHasPromotion() {
    return promotions != null
        && promotions.stream().anyMatch(p -> p.getActive() && p.isCurrentlyValid());
  }

  @Transient
  public String getPromotionBadge() {
    if (Boolean.FALSE.equals(getHasPromotion())) return null;

    Promotion activePromo =
        promotions.stream()
            .filter(Promotion::getActive)
            .filter(Promotion::isCurrentlyValid)
            .findFirst()
            .orElse(null);

    if (activePromo != null) {
      if (activePromo.getDiscountType() == PromotionType.BUY_X_GET_Y) {
        return "BUY "
            + activePromo.getBuyQuantity()
            + " GET "
            + activePromo.getGetQuantity()
            + " FREE";
      } else if (activePromo.getDiscountValue() != null) {
        return activePromo.getDiscountValue() + "% OFF";
      }
    }
    return "SALE";
  }

  @Transient
  public Boolean getQuickAddAvailable() {
    return getInStock() && !getHasPromotion(); // Quick add only for simple products
  }
}
