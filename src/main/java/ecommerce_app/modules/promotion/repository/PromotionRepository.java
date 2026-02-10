package ecommerce_app.modules.promotion.repository;

import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

  Optional<Promotion> findByCode(String code);

  List<Promotion> findByActiveTrue();

  /** Find all active promotions within valid date range */
  @Query(
      "SELECT p FROM Promotion p WHERE p.active = true "
          + "AND (p.startAt IS NULL OR p.startAt <= :now) "
          + "AND (p.endAt IS NULL OR p.endAt >= :now) "
          + "ORDER BY p.createdAt DESC")
  Page<Promotion> findActivePromotions(@Param("now") LocalDateTime now, Pageable pageable);

  /** Find all active promotions within valid date range */
  @Query(
      "SELECT p FROM Promotion p WHERE p.active = true "
          + "AND (p.startAt IS NULL OR p.startAt <= :now) "
          + "AND (p.endAt IS NULL OR p.endAt >= :now) "
          + "ORDER BY p.createdAt DESC")
  List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

  @Query(
      "SELECT p FROM Promotion p JOIN p.products prod WHERE prod.id = :productId "
          + "AND p.active = true AND "
          + "(p.startAt IS NULL OR p.startAt <= :now) AND "
          + "(p.endAt IS NULL OR p.endAt >= :now)")
  List<Promotion> findActivePromotionsByProductId(
      @Param("productId") Long productId, @Param("now") LocalDateTime now);

  @Query("SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId")
  Long countUsagesByPromotionId(@Param("promotionId") Long promotionId);

  /** Find upcoming promotions (not yet started) */
  @Query(
      "SELECT p FROM Promotion p WHERE p.active = true "
          + "AND p.startAt > :now "
          + "ORDER BY p.startAt ASC")
  Page<Promotion> findUpcomingPromotions(@Param("now") LocalDateTime now, Pageable pageable);

  /** Find the best promotions (highest discount, currently active) */
  @Query(
      "SELECT p FROM Promotion p WHERE p.active = true "
          + "AND (p.startAt IS NULL OR p.startAt <= :now) "
          + "AND (p.endAt IS NULL OR p.endAt >= :now) "
          + "AND p.discountValue IS NOT NULL "
          + "ORDER BY p.discountValue DESC")
  Page<Promotion> findBestPromotions(@Param("now") LocalDateTime now, Pageable pageable);

  /** Find promotions by discount type */
  @Query(
      "SELECT p FROM Promotion p WHERE p.discountType = :discountType "
          + "AND p.active = true "
          + "AND (p.startAt IS NULL OR p.startAt <= :now) "
          + "AND (p.endAt IS NULL OR p.endAt >= :now)")
  Page<Promotion> findByDiscountType(
      @Param("discountType") String discountType,
      @Param("now") LocalDateTime now,
      Pageable pageable);

  /** Check if code exists (for validation) */
  boolean existsByCode(String code);

  /** Check if code exists excluding specific ID (for updates) */
  @Query(
      "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Promotion p "
          + "WHERE p.code = :code AND p.id != :id")
  boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);

  /** Count active promotions */
  @Query(
      "SELECT COUNT(p) FROM Promotion p WHERE p.active = true "
          + "AND (p.startAt IS NULL OR p.startAt <= :now) "
          + "AND (p.endAt IS NULL OR p.endAt >= :now)")
  long countActivePromotions(@Param("now") LocalDateTime now);
}
