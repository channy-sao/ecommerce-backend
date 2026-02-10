package ecommerce_app.modules.review.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductReviewSummaryResponse {
  private Long reviewCount;
  private Double averageRating;
}
