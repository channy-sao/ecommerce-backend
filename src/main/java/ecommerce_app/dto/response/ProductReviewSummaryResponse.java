package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Product review summary response")
public class ProductReviewSummaryResponse {

  @Schema(description = "Total number of reviews for the product", example = "120")
  private Long reviewCount;

  @Schema(description = "Average rating of the product out of 5", example = "4.5")
  private Double averageRating;
}
