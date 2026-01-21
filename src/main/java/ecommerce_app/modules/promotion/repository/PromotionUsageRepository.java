package ecommerce_app.modules.promotion.repository;

import ecommerce_app.modules.promotion.model.entity.PromotionUsage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

  Optional<PromotionUsage> findByPromotionIdAndOrderId(Long promotionId, Long orderId);

  @Query("SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId")
  Long countByPromotionId(@Param("promotionId") Long promotionId);

  @Query(
      "SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId AND pu.user.id = :userId")
  Long countByPromotionIdAndUserId(
      @Param("promotionId") Long promotionId, @Param("userId") Long userId);
}
