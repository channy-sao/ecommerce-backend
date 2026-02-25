package ecommerce_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.constant.enums.StockStatus;
import ecommerce_app.constant.enums.WarrantyType;
import ecommerce_app.constant.enums.WarrantyUnit;
import ecommerce_app.entity.base.SoftDeletableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
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

  @Column(nullable = false, name = "is_feature")
  private Boolean isFeature;

  @Column(nullable = false, name = "favorites_count")
  private Integer favoritesCount = 0;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false,
      targetEntity = Category.class,
      cascade = CascadeType.MERGE)
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

  @ManyToMany(mappedBy = "products")
  @JsonIgnore
  private List<Promotion> promotions;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  private List<Review> reviews;

  // NEW: replaces single image field
  // CascadeType.ALL + orphanRemoval = true means:
  //   - when product is saved, images are saved
  //   - when an image is removed from this list, it is deleted from DB automatically
  @OneToMany(
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = "product",
      orphanRemoval = true)
  @JsonIgnore
  private List<ProductImage> images = new ArrayList<>();

  @OneToMany(
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = "product",
      orphanRemoval = true)
  @JsonIgnore
  private List<ProductSpec> specs = new ArrayList<>();

  // Inside Product.java — add after category field

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = true, // brand is optional
      targetEntity = Brand.class,
      cascade = CascadeType.MERGE)
  @JoinColumn(name = "brand_id", referencedColumnName = "id", nullable = true)
  @OnDelete(
      action = OnDeleteAction.SET_NULL) // if brand deleted, product keeps existing, brand_id = null
  @JsonIgnore
  private Brand brand;

  // Warranty
  @Enumerated(EnumType.STRING)
  @Column(name = "warranty_type", length = 20)
  @Builder.Default
  private WarrantyType warrantyType = WarrantyType.NONE;

  @Column(name = "warranty_duration")
  private Integer warrantyDuration; // e.g. 12

  @Enumerated(EnumType.STRING)
  @Column(name = "warranty_unit", length = 10)
  private WarrantyUnit warrantyUnit; // MONTHS

  @Column(name = "warranty_description", length = 500)
  private String warrantyDescription; // "Covers all hardware defects"

  @Transient
  public List<String> getSpecTexts() {
    if (specs == null) return List.of();
    return specs.stream()
        .sorted(Comparator.comparing(ProductSpec::getSortOrder))
        .map(ProductSpec::getSpecText)
        .toList();
  }

  // --- Transient helpers (raw filename only, no URL resolution) ---
  // These are used ONLY by the service layer for file deletion logic.
  // URL resolution is done in ProductMapper.

  @Transient
  public String getPrimaryImagePath() {
    if (images == null || images.isEmpty()) return null;
    return images.stream()
        .min(Comparator.comparing(ProductImage::getSortOrder))
        .map(ProductImage::getImagePath)
        .orElse(null);
  }

  @Transient
  public List<String> getImagePaths() {
    if (images == null) return List.of();
    return images.stream()
        .sorted(Comparator.comparing(ProductImage::getSortOrder))
        .map(ProductImage::getImagePath)
        .toList();
  }

  // --- Unchanged transient methods below ---

  @Transient
  public int getStockQuantity() {
    if (stock == null) return 0;
    return stock.getQuantity();
  }

  @Transient
  public String getShortDescription() {
    if (description == null || description.length() <= 100) return description;
    return description.substring(0, 97) + "...";
  }

  @Transient
  public Boolean getInStock() {
    return getStockQuantity() > 0;
  }

  @Transient
  public StockStatus getStockStatus() {
    int quantity = getStockQuantity();
    if (quantity <= 0) return StockStatus.OUT_OF_STOCK;
    if (quantity <= 10) return StockStatus.LOW_STOCK;
    return StockStatus.IN_STOCK;
  }

  @Transient
  public BigDecimal getDiscountedPrice() {
    if (promotions == null || promotions.isEmpty()) return price;

    return promotions.stream()
        .filter(Promotion::getActive)
        .filter(Promotion::isCurrentlyValid)
        .filter(p -> p.getDiscountValue() != null)
        .max(Comparator.comparing(p -> calculateSaving(p, price)))
        .map(
            promo ->
                switch (promo.getDiscountType()) {
                  case PERCENTAGE -> {
                    BigDecimal discount =
                        price
                            .multiply(promo.getDiscountValue())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    yield price.subtract(discount);
                  }
                  case FIXED_AMOUNT -> {
                    BigDecimal discounted2 = price.subtract(promo.getDiscountValue());
                    yield discounted2.compareTo(BigDecimal.ZERO) < 0
                        ? BigDecimal.ZERO
                        : discounted2;
                  }
                  default -> price;
                })
        .orElse(price);
  }

  @Transient
  public Integer getDiscountPercentage() {
    BigDecimal discounted = getDiscountedPrice();
    // Only calculate if actually discounted
    if (discounted.compareTo(price) < 0 && price.compareTo(BigDecimal.ZERO) > 0) {
      return price
          .subtract(discounted)
          .divide(price, 2, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf(100))
          .intValue();
    }
    return null;
  }

  private BigDecimal calculateSaving(Promotion promo, BigDecimal price) {
    return switch (promo.getDiscountType()) {
      case PERCENTAGE ->
          price
              .multiply(promo.getDiscountValue())
              .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
      case FIXED_AMOUNT -> promo.getDiscountValue().min(price);
      default -> BigDecimal.ZERO;
    };
  }

  @Transient
  public Boolean getHasPromotion() {
    return promotions != null
        && promotions.stream().anyMatch(p -> p.getActive() && p.isCurrentlyValid());
  }

  @Transient
  public String getPromotionBadge() {
    Integer pct = getDiscountPercentage();
    if (pct != null) {
      return pct + "% OFF";
    }
    return null;
  }

  @Transient
  public Boolean getQuickAddAvailable() {
    return getInStock() && !getHasPromotion();
  }
}
