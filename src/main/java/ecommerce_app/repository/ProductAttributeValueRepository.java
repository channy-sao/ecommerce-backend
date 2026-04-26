package ecommerce_app.repository;

import ecommerce_app.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductAttributeValueRepository
    extends JpaRepository<ProductAttributeValue, Long> {


  List<ProductAttributeValue> findByDefinitionIdOrderByDisplayOrderAsc(Long definitionId);

  List<ProductAttributeValue> findByDefinitionIdAndIsActiveTrueOrderByDisplayOrderAsc(
      Long definitionId);

  boolean existsByDefinitionIdAndValueIgnoreCase(Long definitionId, String value);

  long countByDefinitionId(Long definitionId);

  @Query(
      "SELECT COALESCE(MAX(v.displayOrder), 0) FROM ProductAttributeValue v WHERE v.definition.id = :definitionId")
  int findMaxDisplayOrderByDefinitionId(@Param("definitionId") Long definitionId);

  @Query("SELECT v FROM ProductAttributeValue v WHERE v.definition.name = :definitionName AND v.isActive = true")
  List<ProductAttributeValue> findByDefinitionName(@Param("definitionName") String definitionName);
}
