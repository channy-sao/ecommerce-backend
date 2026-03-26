package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Schema(description = "Product review response")
public class ReviewResponse {

  @Schema(description = "Rating given by the user out of 5", example = "4")
  private Integer rating;

  @Schema(
      description = "Review comment left by the user",
      example = "Great product, highly recommend!")
  private String comment;

  @Schema(description = "Username of the reviewer", example = "john_doe")
  private String username;

  @Schema(description = "Date and time the review was submitted", example = "2024-01-01T10:00:00")
  private LocalDateTime createdAt;
}
