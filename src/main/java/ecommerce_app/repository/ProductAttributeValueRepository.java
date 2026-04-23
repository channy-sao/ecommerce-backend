package ecommerce_app.repository;

import ecommerce_app.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {

    // Find all active values for a definition (e.g. all Colors)
    List<ProductAttributeValue> findByDefinitionIdAndIsActiveTrueOrderByDisplayOrderAsc(Long definitionId);

    // Find all active values across all definitions
    List<ProductAttributeValue> findByIsActiveTrueOrderByDefinitionIdAscDisplayOrderAsc();

    // Check duplicate value under the same definition
    boolean existsByDefinitionIdAndValueIgnoreCase(Long definitionId, String value);

    // Check duplicate excluding self (for update)
    boolean existsByDefinitionIdAndValueIgnoreCaseAndIdNot(Long definitionId, String value, Long id);

    // Find by definition name (e.g. "Color")
    @Query("""
        SELECT v FROM ProductAttributeValue v
        JOIN v.definition d
        WHERE LOWER(d.name) = LOWER(:definitionName)
        AND v.isActive = true
        ORDER BY v.displayOrder ASC
    """)
    List<ProductAttributeValue> findByDefinitionName(@Param("definitionName") String definitionName);

    // Find all values used by a specific product's variants
    @Query("""
        SELECT DISTINCT av FROM ProductAttributeValue av
        JOIN av.variants v
        WHERE v.product.id = :productId
        AND v.isActive = true
    """)
    List<ProductAttributeValue> findUsedByProduct(@Param("productId") Long productId);

    // Find all values for given IDs that are active
    @Query("SELECT v FROM ProductAttributeValue v WHERE v.id IN :ids AND v.isActive = true")
    List<ProductAttributeValue> findAllActiveByIds(@Param("ids") List<Long> ids);
}