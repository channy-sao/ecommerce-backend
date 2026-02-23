package ecommerce_app.repository;

import ecommerce_app.entity.ProductView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

  // Check if user already viewed this product
  boolean existsByUserIdAndProductId(Long userId, Long productId);

  // Get categories user viewed most in last N days
  @Query(
      """
        SELECT pv.product.category.id FROM ProductView pv
        WHERE pv.user.id = :userId
        AND pv.viewedAt >= :since
        GROUP BY pv.product.category.id
        ORDER BY COUNT(pv.id) DESC
        """)
  List<Long> findTopCategoryIdsByUserId(
      @Param("userId") Long userId, @Param("since") LocalDateTime since, Pageable pageable);
}
