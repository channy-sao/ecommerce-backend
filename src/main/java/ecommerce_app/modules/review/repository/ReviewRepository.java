package ecommerce_app.modules.review.repository;

import ecommerce_app.modules.review.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  boolean existsByProductIdAndUserId(Long productId, Long userId);

  @Query(
      """
    SELECT AVG(r.rating)
    FROM Review r
    WHERE r.product.id = :productId
      AND r.approved = true
  """)
  Double getAverageRating(@Param("productId") Long productId);

  @Query(
      """
    SELECT COUNT(r)
    FROM Review r
    WHERE r.product.id = :productId
      AND r.approved = true
  """)
  Long countApprovedReviews(@Param("productId") Long productId);

  Page<Review> findByProductIdAndApprovedTrue(Long productId, Pageable pageable);

  @Query(
      """
    SELECT r.rating, COUNT(r)
    FROM Review r
    WHERE r.product.id = :productId
      AND r.approved = true
    GROUP BY r.rating
  """)
  List<Object[]> countByRating(@Param("productId") Long productId);
}
