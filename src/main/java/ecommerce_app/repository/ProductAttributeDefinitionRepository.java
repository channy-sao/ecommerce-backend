package ecommerce_app.repository;

import ecommerce_app.entity.ProductAttributeDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductAttributeDefinitionRepository
    extends JpaRepository<ProductAttributeDefinition, Long> {
  List<ProductAttributeDefinition> findByIsActiveTrue();

  boolean existsByNameIgnoreCase(String name);

  Optional<ProductAttributeDefinition> findByNameIgnoreCase(String name);
}
