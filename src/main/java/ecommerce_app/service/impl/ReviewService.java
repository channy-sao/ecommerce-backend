package ecommerce_app.service.impl;

import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.entity.Product;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.dto.request.CreateReviewRequest;
import ecommerce_app.dto.response.ProductReviewSummaryResponse;
import ecommerce_app.dto.response.ReviewCountResponse;
import ecommerce_app.dto.response.ReviewResponse;
import ecommerce_app.entity.ProductReviewSummary;
import ecommerce_app.entity.Review;
import ecommerce_app.repository.ProductReviewSummaryRepository;
import ecommerce_app.repository.ReviewRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final ProductRepository productRepository;
  private final ProductReviewSummaryRepository summaryRepository;

  // ================= CREATE REVIEW =================
  @Transactional
  public void createReview(Long productId, CreateReviewRequest request, Long userId) {

    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

    // one review per user per product
    if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
      throw new BadRequestException("You have already reviewed this product");
    }

    Review review = new Review();
    review.setProduct(product);
    review.setUserId(userId);
    review.setRating(request.getRating());
    review.setComment(request.getComment());

    reviewRepository.save(review);

    // update summary immediately on save (no need to wait for approval)
    updateSummary(product, review.getRating());
  }

  // ================= GET PAGINATED REVIEWS =================
  @Transactional(readOnly = true)
  public Page<ReviewResponse> getReviews(Long productId, int page, int size) {

    return reviewRepository
        .findByProductId(productId, PageRequest.of(page, size))
        .map(
            r ->
                ReviewResponse.builder()
                    .rating(r.getRating())
                    .comment(r.getComment())
                    .username(r.getUsername())
                    .createdAt(LocalDateTime.from(r.getCreatedAt()))
                    .build());
  }

  // ================= REVIEW COUNT + BREAKDOWN =================
  @Transactional(readOnly = true)
  public ReviewCountResponse getReviewCount(Long productId) {

    Long total = reviewRepository.countReviews(productId);
    Double avg = reviewRepository.getAverageRating(productId);
    List<Object[]> breakdown = reviewRepository.countByRating(productId);

    Map<Integer, Long> stars = new HashMap<>();
    for (int i = 1; i <= 5; i++) {
      stars.put(i, 0L);
    }

    for (Object[] row : breakdown) {
      stars.put((Integer) row[0], (Long) row[1]);
    }

    return ReviewCountResponse.builder()
        .totalReviews(total)
        .averageRating(
            avg == null
                ? 0.0
                : BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue())
        .oneStar(stars.get(1))
        .twoStar(stars.get(2))
        .threeStar(stars.get(3))
        .fourStar(stars.get(4))
        .fiveStar(stars.get(5))
        .build();
  }

  // ================= GET SUMMARY =================
  @Transactional(readOnly = true)
  public ProductReviewSummaryResponse getSummary(Long productId) {

    ProductReviewSummary summary = summaryRepository.findById(productId).orElse(null);

    if (summary == null) {
      return new ProductReviewSummaryResponse(0L, 0.0);
    }

    return new ProductReviewSummaryResponse(
        summary.getReviewCount(), summary.getAverageRating().doubleValue());
  }

  // ================= PRIVATE HELPERS =================
  private void updateSummary(Product product, int newRating) {
    ProductReviewSummary summary =
        summaryRepository.findById(product.getId()).orElseGet(() -> createEmptySummary(product));

    long oldCount = summary.getReviewCount();
    long newCount = oldCount + 1;

    if (newCount <= 0) {
      summary.setReviewCount(0L);
      summary.setAverageRating(BigDecimal.ZERO);
    } else {
      BigDecimal newAvg =
          summary
              .getAverageRating()
              .multiply(BigDecimal.valueOf(oldCount))
              .add(BigDecimal.valueOf(newRating))
              .divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);
      summary.setReviewCount(newCount);
      summary.setAverageRating(newAvg);
    }

    summaryRepository.save(summary);
  }

  // ================= HELPERS =================
  private ProductReviewSummary createEmptySummary(Product product) {
    ProductReviewSummary summary = new ProductReviewSummary();
    summary.setProduct(product);
    summary.setReviewCount(0L);
    summary.setAverageRating(BigDecimal.ZERO);
    return summary;
  }
}
