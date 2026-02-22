package ecommerce_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductReviewSummaryResponse {
  private Long reviewCount;
  private Double averageRating;
}
