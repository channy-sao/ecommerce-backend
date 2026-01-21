package ecommerce_app.modules.stock.repository;

import ecommerce_app.modules.stock.model.entity.ProductImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductImportRepository extends JpaRepository<ProductImport, Long>, JpaSpecificationExecutor<ProductImport> {
    List<ProductImport> findByProductId(Long productId);
}
