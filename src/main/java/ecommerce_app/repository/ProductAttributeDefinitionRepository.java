package ecommerce_app.repository;

import ecommerce_app.entity.ProductAttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAttributeDefinitionRepository extends JpaRepository<ProductAttributeDefinition, Long> {
}