package ecommerce_app.repository;

import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.entity.ProductVariant;
import ecommerce_app.entity.VariantStockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductIdAndIsActiveTrue(Long productId);
    Optional<ProductVariant> findByProductIdAndIsDefaultTrue(Long productId);

    Optional<ProductVariant> findByProductIdAndIsDefault(Long productId, Boolean isDefault);

    List<ProductVariant> findByProductId(Long productId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.stockQuantity <= v.lowStockThreshold AND v.isActive = true")
    List<ProductVariant> findLowStockByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(v.stockQuantity) FROM ProductVariant v WHERE v.product.id = :productId AND v.isActive = true")
    Integer sumStockByProductId(@Param("productId") Long productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.stockQuantity <= :threshold AND v.isActive = true")
    List<ProductVariant> findByStockQuantityLessThanEqual(@Param("threshold") int threshold);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.stockQuantity <= :threshold AND v.isActive = true")
    List<ProductVariant> findByProductIdAndStockQuantityLessThanEqual(
            @Param("productId") Long productId,
            @Param("threshold") int threshold
    );

    // Get total stock for a product
    @Query("SELECT COALESCE(SUM(v.stockQuantity), 0) FROM ProductVariant v WHERE v.product.id = :productId AND v.isActive = true")
    Integer getTotalStockByProductId(@Param("productId") Long productId);

    Long countByProductId(Long productId);

    Long countByProductIdAndIsDefaultTrue(Long productId);
}