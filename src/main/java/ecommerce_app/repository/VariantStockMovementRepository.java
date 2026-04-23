package ecommerce_app.repository;

import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.entity.VariantStockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantStockMovementRepository extends JpaRepository<VariantStockMovement, Long> {
    List<VariantStockMovement> findByVariantIdOrderByCreatedAtDesc(Long variantId);
    List<VariantStockMovement> findByVariantIdAndMovementType(Long variantId, StockMovementType type);
}