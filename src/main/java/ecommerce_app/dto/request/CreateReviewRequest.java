package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CreateReviewRequest", description = "Request object for creating a product review")
public class CreateReviewRequest {

  @NotNull(message = "Rating is required")
  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must not exceed 5")
  @Schema(description = "Rating from 1 to 5", example = "5", minimum = "1", maximum = "5")
  private Integer rating;

  @Size(max = 1000, message = "Comment must not exceed 1000 characters")
  @Schema(description = "Optional review comment", example = "Great product, highly recommended!")
  private String comment;
}