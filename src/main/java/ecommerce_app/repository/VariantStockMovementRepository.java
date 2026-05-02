package ecommerce_app.repository;

import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.entity.ProductVariant;
import ecommerce_app.entity.VariantStockMovement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VariantStockMovementRepository extends JpaRepository<VariantStockMovement, Long> {
  List<VariantStockMovement> findByVariantIdOrderByCreatedAtDesc(Long variantId);

  List<VariantStockMovement> findByVariantIdAndCreatedAtBetween(
      Long variantId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  @Query(
      "SELECT m FROM VariantStockMovement m WHERE m.variant.id IN :variantIds AND m.createdAt BETWEEN :startDate AND :endDate")
  List<VariantStockMovement> findByVariantIdInAndCreatedAtBetween(
      @Param("variantIds") List<Long> variantIds,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  // Get low stock variants
  @Query("SELECT v FROM ProductVariant v WHERE v.stockQuantity <= :threshold AND v.isActive = true")
  List<ProductVariant> findByStockQuantityLessThanEqual(@Param("threshold") int threshold);

  // Get stock movements by reference
  List<VariantStockMovement> findByReferenceTypeAndReferenceId(
      String referenceType, Long referenceId);

  List<VariantStockMovement> findByVariantIsNullAndCreatedAtBetween(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  List<VariantStockMovement> findByVariantIdAndMovementType(Long variantId, StockMovementType type);

}
