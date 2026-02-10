package ecommerce_app.modules.review.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReviewCountResponse {

  private Long totalReviews;
  private Double averageRating;

  // breakdown
  private Long oneStar;
  private Long twoStar;
  private Long threeStar;
  private Long fourStar;
  private Long fiveStar;
}
