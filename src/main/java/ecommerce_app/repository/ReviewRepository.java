package ecommerce_app.repository;

import ecommerce_app.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  Page<Review> findByProductId(Long productId, Pageable pageable);

  boolean existsByProductIdAndUserId(Long productId, Long userId);

  @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
  Long countReviews(@Param("productId") Long productId);

  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
  Double getAverageRating(@Param("productId") Long productId);

  @Query(
      "SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating")
  List<Object[]> countByRating(@Param("productId") Long productId);
}
