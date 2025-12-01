package ecommerce_app.modules.stock.repository;

import ecommerce_app.modules.stock.model.entity.ProductImport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImportRepository extends JpaRepository<ProductImport, Long> {
    List<ProductImport> findByProductId(Long productId);
}
