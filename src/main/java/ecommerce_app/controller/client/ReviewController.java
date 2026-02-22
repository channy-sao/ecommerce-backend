package ecommerce_app.controller.client;

import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.request.CreateReviewRequest;
import ecommerce_app.dto.response.ProductReviewSummaryResponse;
import ecommerce_app.dto.response.ReviewCountResponse;
import ecommerce_app.dto.response.ReviewResponse;
import ecommerce_app.service.impl.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
  public ResponseEntity<BaseBodyResponse<Void>> createReview(
      @PathVariable Long productId,
      @RequestBody CreateReviewRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {

    reviewService.createReview(productId, request, user.getId());

    return BaseBodyResponse.success("Review submitted and awaiting approval");
  }

  // ================= GET PAGINATED REVIEWS =================
  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<ReviewResponse>>> getReviews(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    return BaseBodyResponse.pageSuccess(
        reviewService.getReviews(productId, page, size), "Get reviews successfully");
  }

  // ================= GET REVIEW SUMMARY =================
  @GetMapping("/summary")
  public ResponseEntity<BaseBodyResponse<ProductReviewSummaryResponse>> getReviewSummary(
      @PathVariable Long productId) {

    return BaseBodyResponse.success(
        reviewService.getSummary(productId), "Get review summary successfully");
  }

  // ================= GET FULL REVIEW COUNT (BREAKDOWN) =================
  @GetMapping("/count")
  public ResponseEntity<BaseBodyResponse<ReviewCountResponse>> getReviewCount(
      @PathVariable Long productId) {

    return BaseBodyResponse.success(
        reviewService.getReviewCount(productId), "Get review count successfully");
  }

  @PutMapping("/{reviewId}/approve")
  public ResponseEntity<BaseBodyResponse<Void>> approveReview(@PathVariable Long reviewId) {

    reviewService.approveReview(reviewId);

    return BaseBodyResponse.success("Review approved successfully");
  }
}
