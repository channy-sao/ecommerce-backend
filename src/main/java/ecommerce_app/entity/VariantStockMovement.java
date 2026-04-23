package ecommerce_app.entity;

import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.entity.base.UserAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "variant_stock_movements",
    indexes = {
        @Index(name = "idx_stock_movement_variant",    columnList = "variant_id"),
        @Index(name = "idx_stock_movement_type",       columnList = "movement_type"),
        @Index(name = "idx_stock_movement_created_at", columnList = "created_at")
    }
)
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class VariantStockMovement extends UserAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private StockMovementType movementType; // IN, OUT, ADJUSTMENT, RETURN

    @Column(name = "quantity", nullable = false)
    private Integer quantity;             // always positive

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;       // snapshot before change

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;        // snapshot after change

    @Column(name = "reference_id")
    private Long referenceId;             // order_id, import_id, etc.

    @Column(name = "reference_type", length = 50)
    private String referenceType;         // "ORDER", "IMPORT", "MANUAL"

    @Column(name = "note", length = 500)
    private String note;
}