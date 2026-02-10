package ecommerce_app.modules.product.repository;

import ecommerce_app.modules.product.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  Optional<Product> findByName(String name);

  Optional<Product> findByUuid(UUID uuid);
}
