package ecommerce_app.modules.product.repository;

import ecommerce_app.modules.product.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  Optional<Product> findByName(String name);

  Optional<Product> findByUuid(UUID uuid);

  /** Find featured products */
  Page<Product> findByIsFeatureTrue(Pageable pageable);

  /** Find products by category ID */
  Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

  /** Search products by name or description (case-insensitive) */
  Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
      String name, String description, Pageable pageable);

  /**
   * Find products with active promotions Uses custom query to join promotions and filter by active
   * status
   */
  @Query(
      "SELECT DISTINCT p FROM Product p "
          + "LEFT JOIN p.promotions promo "
          + "WHERE promo.active = true "
          + "AND (promo.startAt IS NULL OR promo.startAt <= CURRENT_TIMESTAMP) "
          + "AND (promo.endAt IS NULL OR promo.endAt >= CURRENT_TIMESTAMP)")
  Page<Product> findProductsWithActivePromotions(Pageable pageable);

  /** Find products by category and featured status */
  Page<Product> findByCategoryIdAndIsFeatureTrue(Long categoryId, Pageable pageable);

  /** Count products by category */
  long countByCategoryId(Long categoryId);

  /** Check if product exists by name (for validation) */
  boolean existsByName(String name);

  /** Check if product exists by name excluding specific ID (for updates) */
  @Query(
      "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p "
          + "WHERE p.name = :name AND p.id != :id")
  boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

  @Query("""
       SELECT p FROM Product p
       JOIN p.stock s
       WHERE s.quantity > 0
       AND s.quantity <= :threshold
       """)
  Page<Product> findLowStockProducts(
          @Param("threshold") int threshold,
          Pageable pageable
  );

}
