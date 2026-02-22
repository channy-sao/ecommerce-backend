package ecommerce_app.repository;

import ecommerce_app.entity.ProductSpec;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {

  List<ProductSpec> findByProductIdOrderBySortOrderAsc(Long productId);

  void deleteAllByProductId(Long productId);
}
