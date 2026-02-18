package ecommerce_app.modules.product.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "product_images",
    indexes = @Index(columnList = "product_id", name = "product_images_product_id_index"))
@Getter
@Setter
public class ProductImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 500, name = "image_path")
  private String imagePath; // stores raw filename only e.g. "abc123.png"

  @Column(nullable = false, name = "sort_order")
  private Integer sortOrder = 0; // 0 = primary image

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;
}
