package ecommerce_app.entity;

import ecommerce_app.entity.base.TimeAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(
    name = "product_attribute_definitions",
    indexes = @Index(name = "idx_attr_def_name", columnList = "name")
)
@Getter
@Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class ProductAttributeDefinition extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;              // "Color", "Size", "Material"

    @Column(name = "display_name", length = 100)
    private String displayName;       // "Color", "Size"

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAttributeValue> values;
}