package ecommerce_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.entity.base.TimeAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(
    name = "product_attribute_values",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_attr_value",
            columnNames = {"product_attribute_id", "value"}),
    indexes = @Index(name = "idx_attr_value", columnList = "product_attribute_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValue extends TimeAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_attribute_id", nullable = false)
  private ProductAttribute productAttribute;

  @Column(nullable = false, length = 100)
  private String value; // "Red", "XL", "Blue"

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private Integer displayOrder = 0;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @ManyToMany(mappedBy = "attributeValues", fetch = FetchType.LAZY)
  @JsonIgnore
  private List<ProductVariant> variants;
}
