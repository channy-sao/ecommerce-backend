package ecommerce_app.modules.review.model.entity;

import ecommerce_app.modules.product.model.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product_review_summary")
@Getter
@Setter
@NoArgsConstructor
public class ProductReviewSummary {

  @Id
  private Long productId;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @Column(nullable = false)
  private Long reviewCount = 0L;

  @Column(nullable = false, precision = 3, scale = 2)
  private BigDecimal averageRating = BigDecimal.ZERO;
}
