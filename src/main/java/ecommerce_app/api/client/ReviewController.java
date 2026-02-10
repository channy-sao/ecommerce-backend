package ecommerce_app.api.client;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.review.model.dto.CreateReviewRequest;
import ecommerce_app.modules.review.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/products/{productId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Mobile Review Controller", description = "Review APIs for mobile app")
public class ReviewController {

  private final ReviewService reviewService;

  // ================= CREATE REVIEW =================
  @PostMapping
  public ResponseEntity<BaseBodyResponse> createReview(
      @PathVariable Long productId,
      @RequestBody CreateReviewRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    reviewService.createReview(productId, request, user.getId());

    return BaseBodyResponse.success(null, "Review submitted and awaiting approval");
  }

  // ================= GET PAGINATED REVIEWS =================
  @GetMapping
  public ResponseEntity<BaseBodyResponse> getReviews(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    return BaseBodyResponse.success(
        reviewService.getReviews(productId, page, size), "Get reviews successfully");
  }

  // ================= GET REVIEW SUMMARY =================
  @GetMapping("/summary")
  public ResponseEntity<BaseBodyResponse> getReviewSummary(@PathVariable Long productId) {

    return BaseBodyResponse.success(
        reviewService.getSummary(productId), "Get review summary successfully");
  }

  // ================= GET FULL REVIEW COUNT (BREAKDOWN) =================
  @GetMapping("/count")
  public ResponseEntity<BaseBodyResponse> getReviewCount(@PathVariable Long productId) {

    return BaseBodyResponse.success(
        reviewService.getReviewCount(productId), "Get review count successfully");
  }

  @PutMapping("/{reviewId}/approve")
  public ResponseEntity<BaseBodyResponse> approveReview(@PathVariable Long reviewId) {

    reviewService.approveReview(reviewId);

    return BaseBodyResponse.success(null, "Review approved successfully");
  }
}
