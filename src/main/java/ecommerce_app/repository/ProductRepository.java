package ecommerce_app.repository;

import ecommerce_app.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

  Optional<Product> findByName(String name);

  Optional<Product> findByUuid(UUID uuid);

  Optional<Product> findByCode(String code);

  Page<Product> findByIsFeatureTrue(Pageable pageable);

  Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

  Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
      String name, String description, Pageable pageable);

  @Modifying
  @Query("UPDATE Product p SET p.code = :code WHERE p.id = :id")
  void updateCode(Long id, String code);

  @Query(
      "SELECT DISTINCT p FROM Product p "
          + "LEFT JOIN p.promotions promo "
          + "WHERE promo.active = true "
          + "AND (promo.startAt IS NULL OR promo.startAt <= CURRENT_TIMESTAMP) "
          + "AND (promo.endAt IS NULL OR promo.endAt >= CURRENT_TIMESTAMP)")
  Page<Product> findProductsWithActivePromotions(Pageable pageable);

  Page<Product> findByCategoryIdAndIsFeatureTrue(Long categoryId, Pageable pageable);

  long countByCategoryId(Long categoryId);

  boolean existsByName(String name);

  @Query(
      "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p "
          + "WHERE p.name = :name AND p.id != :id")
  boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

  // ════════════════════════════════════════════════════════════════
  // ✅ FIXED: All stock queries now use variants
  // ════════════════════════════════════════════════════════════════

  @Query(
      "SELECT DISTINCT p FROM Product p JOIN p.variants v "
          + "WHERE v.stockQuantity > 0 "
          + "AND v.stockQuantity <= :threshold "
          + "AND v.isActive = true")
  Page<Product> findLowStockProducts(@Param("threshold") int threshold, Pageable pageable);

  @Query(
      "SELECT DISTINCT p FROM Product p "
          + "JOIN FETCH p.variants v "
          + "JOIN FETCH p.category c "
          + "WHERE p.deleted = false "
          + "AND v.stockQuantity > 0 "
          + "AND v.stockQuantity <= :threshold "
          + "AND v.isActive = true "
          + "ORDER BY v.stockQuantity ASC")
  List<Product> findNearEmptyStockProducts(@Param("threshold") int threshold);

  @Query(
      "SELECT COUNT(DISTINCT p) FROM Product p "
          + "JOIN p.variants v "
          + "WHERE p.deleted = false "
          + "AND v.stockQuantity > 0 "
          + "AND v.stockQuantity <= :threshold "
          + "AND v.isActive = true")
  long countNearEmptyStockProducts(@Param("threshold") int threshold);

  @Query(
      "SELECT DISTINCT p FROM Product p "
          + "JOIN p.variants v "
          + "WHERE p.category.id = :categoryId "
          + "AND p.id != :excludeId "
          + "AND v.stockQuantity > 0 "
          + "AND v.isActive = true "
          + "ORDER BY "
          + "  CASE WHEN (:brandId IS NOT NULL AND p.brand.id = :brandId) THEN 0 ELSE 1 END, "
          + "  p.createdAt DESC")
  Page<Product> findRelatedProducts(
      @Param("categoryId") Long categoryId,
      @Param("brandId") Long brandId,
      @Param("excludeId") Long excludeId,
      Pageable pageable);

  @Query(
      "SELECT DISTINCT p FROM Product p "
          + "JOIN p.variants v "
          + "WHERE v.stockQuantity > 0 "
          + "AND v.isActive = true "
          + "ORDER BY p.favoritesCount DESC, p.createdAt DESC")
  Page<Product> findPopularProducts(Pageable pageable);

  @Query(
      "SELECT DISTINCT p FROM Product p "
          + "JOIN p.variants v "
          + "WHERE p.category.id IN :categoryIds "
          + "AND p.id NOT IN :excludeIds "
          + "AND v.stockQuantity > 0 "
          + "AND v.isActive = true "
          + "ORDER BY p.favoritesCount DESC, p.createdAt DESC")
  Page<Product> findRecommendedProducts(
      @Param("categoryIds") List<Long> categoryIds,
      @Param("excludeIds") List<Long> excludeIds,
      Pageable pageable);

  @Query(
      "SELECT DISTINCT p.name FROM Product p "
          + "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) "
          + "ORDER BY p.name "
          + "LIMIT 8")
  List<String> findSuggestions(@Param("q") String q);

  @Query(
      "SELECT DISTINCT p FROM Product p "
          + "JOIN p.variants v "
          + "WHERE p.brand.id = :brandId "
          + "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
          + "AND v.stockQuantity > 0 "
          + "AND v.isActive = true "
          + "ORDER BY p.createdAt DESC")
  Page<Product> findByBrandId(
      @Param("brandId") Long brandId, @Param("search") String search, Pageable pageable);

  @Query(
      "SELECT DISTINCT p FROM Product p "
          + "JOIN p.variants v "
          + "WHERE p.brand.id = :brandId "
          + "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
          + "AND v.stockQuantity > 0 "
          + "AND v.isActive = true "
          + "ORDER BY p.createdAt DESC")
  Page<Product> findByBrandIdForAdmin(
      @Param("brandId") Long brandId, @Param("search") String search, Pageable pageable);
}
