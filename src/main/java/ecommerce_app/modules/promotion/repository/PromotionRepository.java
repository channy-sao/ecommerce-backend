package ecommerce_app.modules.promotion.repository;

import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

  Optional<Promotion> findByCode(String code);

  List<Promotion> findByActiveTrue();

  @Query(
      "SELECT p FROM Promotion p WHERE p.active = true AND "
          + "(p.startAt IS NULL OR p.startAt <= :now) AND "
          + "(p.endAt IS NULL OR p.endAt >= :now)")
  List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

  @Query(
      "SELECT p FROM Promotion p JOIN p.products prod WHERE prod.id = :productId "
          + "AND p.active = true AND "
          + "(p.startAt IS NULL OR p.startAt <= :now) AND "
          + "(p.endAt IS NULL OR p.endAt >= :now)")
  List<Promotion> findActivePromotionsByProductId(
      @Param("productId") Long productId, @Param("now") LocalDateTime now);

  boolean existsByCode(String code);

  @Query("SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId")
  Long countUsagesByPromotionId(@Param("promotionId") Long promotionId);
}
