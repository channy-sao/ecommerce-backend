package ecommerce_app.modules.review.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReviewResponse {
  private Integer rating;
  private String comment;
  private String username;
  private LocalDateTime createdAt;
}
