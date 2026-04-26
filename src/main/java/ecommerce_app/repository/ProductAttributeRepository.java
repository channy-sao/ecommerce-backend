package ecommerce_app.repository;

import ecommerce_app.entity.ProductAttribute;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductAttributeRepository
    extends JpaRepository<ProductAttribute, Long> {
  List<ProductAttribute> findByIsActiveTrue();

  boolean existsByNameIgnoreCase(String name);

  Optional<ProductAttribute> findByNameIgnoreCase(String name);
}
