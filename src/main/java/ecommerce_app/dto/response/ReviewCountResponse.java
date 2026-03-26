package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Review count and rating breakdown for a product")
public class ReviewCountResponse {

  @Schema(description = "Total number of reviews", example = "120")
  private Long totalReviews;

  @Schema(description = "Average rating out of 5", example = "4.3")
  private Double averageRating;

  @Schema(description = "Number of 1-star reviews", example = "5")
  private Long oneStar;

  @Schema(description = "Number of 2-star reviews", example = "8")
  private Long twoStar;

  @Schema(description = "Number of 3-star reviews", example = "15")
  private Long threeStar;

  @Schema(description = "Number of 4-star reviews", example = "32")
  private Long fourStar;

  @Schema(description = "Number of 5-star reviews", example = "60")
  private Long fiveStar;
}
