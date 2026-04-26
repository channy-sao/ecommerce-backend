package ecommerce_app.repository;

import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.entity.ProductVariant;
import ecommerce_app.entity.VariantStockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductIdAndIsActiveTrue(Long productId);

    List<ProductVariant> findByProductId(Long productId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.stockQuantity <= v.lowStockThreshold AND v.isActive = true")
    List<ProductVariant> findLowStockByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(v.stockQuantity) FROM ProductVariant v WHERE v.product.id = :productId AND v.isActive = true")
    Integer sumStockByProductId(@Param("productId") Long productId);
}