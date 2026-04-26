package ecommerce_app.service;

import ecommerce_app.dto.request.ProductAttributeDefinitionRequest;
import ecommerce_app.dto.request.ProductAttributeValueRequest;
import ecommerce_app.dto.response.ProductAttributeDefinitionResponse;
import ecommerce_app.dto.response.ProductAttributeValueResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Service interface for managing product attributes (definitions and values) */
public interface ProductAttributeService {

  // ==================== Attribute Definitions ====================

  /**
   * Get all attribute definitions
   *
   * @return list of attribute definition responses
   */
  List<ProductAttributeDefinitionResponse> getAllDefinitions();

  /**
   * Get attribute definitions with pagination
   *
   * @param pageable pagination information
   * @return page of attribute definition responses
   */
  Page<ProductAttributeDefinitionResponse> getDefinitions(Pageable pageable);

  /**
   * Get active attribute definitions only
   *
   * @return list of active attribute definition responses
   */
  List<ProductAttributeDefinitionResponse> getActiveDefinitions();

  /**
   * Get attribute definition by ID
   *
   * @param definitionId the definition ID
   * @return attribute definition response
   */
  ProductAttributeDefinitionResponse getDefinitionById(Long definitionId);

  /**
   * Create a new attribute definition
   *
   * @param request the attribute definition request
   * @return created attribute definition response
   */
  ProductAttributeDefinitionResponse createDefinition(ProductAttributeDefinitionRequest request);

  /**
   * Update attribute definition
   *
   * @param definitionId the definition ID
   * @param request the updated definition data
   * @return updated attribute definition response
   */
  ProductAttributeDefinitionResponse updateDefinition(
      Long definitionId, ProductAttributeDefinitionRequest request);

  /**
   * Deactivate attribute definition (soft delete)
   *
   * @param definitionId the definition ID
   */
  void deactivateDefinition(Long definitionId);

  /**
   * Activate attribute definition
   *
   * @param definitionId the definition ID
   */
  void activateDefinition(Long definitionId);

  /**
   * Delete attribute definition permanently
   *
   * @param definitionId the definition ID
   */
  void deleteDefinition(Long definitionId);

  // ==================== Attribute Values ====================

  /**
   * Get all values for an attribute definition
   *
   * @param definitionId the definition ID
   * @return list of attribute value responses
   */
  List<ProductAttributeValueResponse> getValuesByDefinition(Long definitionId);

  /**
   * Get active values for an attribute definition
   *
   * @param definitionId the definition ID
   * @return list of active attribute value responses
   */
  List<ProductAttributeValueResponse> getActiveValuesByDefinition(Long definitionId);

  /**
   * Get attribute value by ID
   *
   * @param valueId the value ID
   * @return attribute value response
   */
  ProductAttributeValueResponse getValueById(Long valueId);

  /**
   * Create a new attribute value
   *
   * @param definitionId the definition ID
   * @param request the attribute value request
   * @return created attribute value response
   */
  ProductAttributeValueResponse createValue(
      Long definitionId, ProductAttributeValueRequest request);

  /**
   * Update attribute value
   *
   * @param valueId the value ID
   * @param request the updated value data
   * @return updated attribute value response
   */
  ProductAttributeValueResponse updateValue(Long valueId, ProductAttributeValueRequest request);

  /**
   * Deactivate attribute value (soft delete)
   *
   * @param valueId the value ID
   */
  void deactivateValue(Long valueId);

  /**
   * Activate attribute value
   *
   * @param valueId the value ID
   */
  void activateValue(Long valueId);

  /**
   * Delete attribute value permanently
   *
   * @param valueId the value ID
   */
  void deleteValue(Long valueId);

  /**
   * Reorder attribute values
   *
   * @param definitionId the definition ID
   * @param orderedValueIds ordered list of value IDs
   */
  void reorderValues(Long definitionId, List<Long> orderedValueIds);
}
