package ecommerce_app.entity;

import ecommerce_app.entity.base.TimeAuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(
    name = "brands",
    indexes = {
      @Index(name = "idx_brand_name", columnList = "name"),
      @Index(name = "idx_brand_active", columnList = "is_active"),
      @Index(name = "idx_brand_display_order", columnList = "display_order")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand extends TimeAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @Column(name = "logo", length = 500)
  private String logo; // image path

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private Integer displayOrder = 0;

  @OneToMany(
      mappedBy = "brand",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @OnDelete(action = OnDeleteAction.SET_NULL)
  @JsonIgnore
  private List<Product> products;
}
