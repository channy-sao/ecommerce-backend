package ecommerce_app.repository;

import ecommerce_app.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductAttributeValueRepository
    extends JpaRepository<ProductAttributeValue, Long> {


  List<ProductAttributeValue> findByProductAttributeIdOrderByDisplayOrderAsc(Long attributeId);

  List<ProductAttributeValue> findByProductAttributeIdAndIsActiveTrueOrderByDisplayOrderAsc(
      Long attributeId);

  boolean existsByProductAttributeIdAndValueIgnoreCase(Long attributeId, String value);

  long countByProductAttributeId(Long attributeId);

  @Query(
      "SELECT COALESCE(MAX(v.displayOrder), 0) FROM ProductAttributeValue v WHERE v.productAttribute.id = :attributeId")
  int findMaxDisplayOrderByProductAttributeId(@Param("attributeId") Long attributeId);

  @Query("SELECT v FROM ProductAttributeValue v WHERE v.productAttribute.name = :attributeName AND v.isActive = true")
  List<ProductAttributeValue> findByProductAttributeName(@Param("attributeName") String attributeName);
}
