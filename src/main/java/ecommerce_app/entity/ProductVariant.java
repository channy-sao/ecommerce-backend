package ecommerce_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.constant.enums.StockStatus;
import ecommerce_app.entity.base.UserAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "product_variants",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_variant_sku",
        columnNames = {"sku"}
    ),
    indexes = {
        @Index(name = "idx_variant_product",    columnList = "product_id"),
        @Index(name = "idx_variant_sku",        columnList = "sku"),
        @Index(name = "idx_variant_is_active",  columnList = "is_active")
    }
)
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductVariant extends UserAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;               // "SHIRT-RED-XL"

    // Override product price per variant (null = use product price)
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = Boolean.TRUE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "variant_attribute_values",
        joinColumns = @JoinColumn(name = "variant_id"),
        inverseJoinColumns = @JoinColumn(name = "attribute_value_id")
    )
    @Builder.Default
    private List<ProductAttributeValue> attributeValues = new ArrayList<>();

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VariantStockMovement> stockMovements = new ArrayList<>();

    // ── Transient helpers ────────────────────────────────────────────
    @Transient
    public BigDecimal getEffectivePrice() {
        return price != null ? price : product.getPrice();
    }

    @Transient
    public StockStatus getStockStatus() {
        if (stockQuantity <= 0)              return StockStatus.OUT_OF_STOCK;
        if (stockQuantity <= lowStockThreshold) return StockStatus.LOW_STOCK;
        return StockStatus.IN_STOCK;
    }

    @Transient
    public Boolean getInStock() {
        return stockQuantity > 0;
    }
}