package ecommerce_app.modules.promotion.repository;

import ecommerce_app.modules.promotion.model.entity.PromotionUsage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

  /** Count how many times a user has used a specific promotion */
  long countByPromotionIdAndUserId(Long promotionId, Long userId);

  /** Find all usage records for a promotion */
  List<PromotionUsage> findByPromotionId(Long promotionId);

  /** Find all usage records for a user */
  List<PromotionUsage> findByUserId(Long userId);

  /** Find all usage records for a specific order */
  List<PromotionUsage> findByOrderId(Long orderId);

  /** Check if user has used a promotion */
  boolean existsByPromotionIdAndUserId(Long promotionId, Long userId);

  /** Count total usage for a promotion */
  long countByPromotionId(Long promotionId);

  /** Get usage count by promotion and user */
  @Query(
"""
  SELECT COUNT(pu)
  FROM PromotionUsage pu
  WHERE pu.promotion.id = :promotionId
    AND pu.user.id = :userId
""")
  long getUserUsageCount(@Param("promotionId") Long promotionId, @Param("userId") Long userId);
}
