package ecommerce_app.service.impl;

import ecommerce_app.dto.request.ProductAttributeRequest;
import ecommerce_app.dto.request.ProductAttributeValueRequest;
import ecommerce_app.dto.response.ProductAttributeResponse;
import ecommerce_app.dto.response.ProductAttributeValueResponse;
import ecommerce_app.entity.ProductAttribute;
import ecommerce_app.entity.ProductAttributeValue;
import ecommerce_app.exception.DuplicateResourceException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.ProductAttributeMapper;
import ecommerce_app.repository.ProductAttributeRepository;
import ecommerce_app.repository.ProductAttributeValueRepository;
import ecommerce_app.service.ProductAttributeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductAttributeServiceImpl implements ProductAttributeService {

  private final ProductAttributeRepository attributeRepository;
  private final ProductAttributeValueRepository valueRepository;
  private final ProductAttributeMapper attributeMapper;

  // ==================== Attribute Definitions ====================

  @Override
  public List<ProductAttributeResponse> getAllProductAttributes() {
    log.debug("Fetching all attribute definitions");
    return attributeRepository.findAll().stream().map(attributeMapper::toResponse).toList();
  }

  @Override
  public Page<ProductAttributeResponse> getProductAttributes(Pageable pageable) {
    log.debug("Fetching attribute definitions with pagination: {}", pageable);
    return attributeRepository.findAll(pageable).map(attributeMapper::toResponse);
  }

  @Override
  public List<ProductAttributeResponse> getActiveProductAttributes() {
    log.debug("Fetching active attribute definitions");
    return attributeRepository.findByIsActiveTrue().stream()
        .map(attributeMapper::toResponse)
        .toList();
  }

  @Override
  public ProductAttributeResponse getProductAttributeById(Long definitionId) {
    log.debug("Fetching attribute definition by ID: {}", definitionId);
    return attributeRepository
        .findById(definitionId)
        .map(attributeMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));
  }

  @Override
  @Transactional
  public ProductAttributeResponse createProductAttribute(
      ProductAttributeRequest request) {
    log.info("Creating new attribute definition: {}", request.getName());

    // Check for duplicate name
    if (attributeRepository.existsByNameIgnoreCase(request.getName())) {
      throw new DuplicateResourceException("AttributeDefinition", "name", request.getName());
    }

    ProductAttribute entity = attributeMapper.toEntity(request);
    entity.setIsActive(true);

    ProductAttribute saved = attributeRepository.save(entity);
    log.info("Attribute definition created with ID: {}", saved.getId());

    return attributeMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public ProductAttributeResponse updateProductAttribute(
      Long definitionId, ProductAttributeRequest request) {
    log.info("Updating attribute definition with ID: {}", definitionId);

    ProductAttribute existing =
        attributeRepository
            .findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));

    // Check for duplicate name (excluding self)
    if (request.getName() != null
        && !request.getName().equals(existing.getName())
        && attributeRepository.existsByNameIgnoreCase(request.getName())) {
      throw new DuplicateResourceException("AttributeDefinition", "name", request.getName());
    }

    attributeMapper.updateEntity(existing, request);

    ProductAttribute updated = attributeRepository.save(existing);
    log.info("Attribute definition updated: {}", updated.getId());

    return attributeMapper.toResponse(updated);
  }

  @Override
  @Transactional
  public void deactivateProductAttribute(Long definitionId) {
    log.info("Deactivating attribute definition with ID: {}", definitionId);

    ProductAttribute definition =
        attributeRepository
            .findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));
    definition.setIsActive(false);
    attributeRepository.save(definition);

    log.info("Attribute definition deactivated: {}", definitionId);
  }

  @Override
  @Transactional
  public void activateProductAttribute(Long definitionId) {
    log.info("Activating attribute definition with ID: {}", definitionId);

    ProductAttribute definition =
        attributeRepository
            .findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));
    definition.setIsActive(true);
    attributeRepository.save(definition);

    log.info("Attribute definition activated: {}", definitionId);
  }

  @Override
  @Transactional
  public void deleteDefinition(Long definitionId) {
    log.info("Permanently deleting attribute definition with ID: {}", definitionId);

    ProductAttribute definition =
        attributeRepository
            .findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));

    long valueCount = valueRepository.countByProductAttributeId(definitionId);
    if (valueCount > 0) {
      log.warn(
          "Deleting definition with {} associated values. Values will also be deleted due to cascade.",
          valueCount);
    }

    attributeRepository.delete(definition);
    log.info("Attribute definition deleted: {}", definitionId);
  }

  // ==================== Attribute Values ====================

  @Override
  public List<ProductAttributeValueResponse> getValuesByProductAttribute(Long definitionId) {
    log.debug("Fetching all values for definition ID: {}", definitionId);

    // Verify definition exists
    if (!attributeRepository.existsById(definitionId)) {
      throw new ResourceNotFoundException("AttributeDefinition", definitionId);
    }

    return valueRepository.findByProductAttributeIdOrderByDisplayOrderAsc(definitionId).stream()
        .map(attributeMapper::toValueResponse)
        .toList();
  }

  @Override
  public List<ProductAttributeValueResponse> getActiveValuesByProductAttribute(Long definitionId) {
    log.debug("Fetching active values for definition ID: {}", definitionId);

    // Verify definition exists
    if (!attributeRepository.existsById(definitionId)) {
      throw new ResourceNotFoundException("AttributeDefinition", definitionId);
    }

    return valueRepository
        .findByProductAttributeIdAndIsActiveTrueOrderByDisplayOrderAsc(definitionId)
        .stream()
        .map(attributeMapper::toValueResponse)
        .toList();
  }

  @Override
  public ProductAttributeValueResponse getValueById(Long valueId) {
    log.debug("Fetching attribute value by ID: {}", valueId);
    return valueRepository
        .findById(valueId)
        .map(attributeMapper::toValueResponse)
        .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", valueId));
  }

  @Override
  @Transactional
  public ProductAttributeValueResponse createValue(
      Long definitionId, ProductAttributeValueRequest request) {
    log.info("Creating new attribute value for definition ID: {}", definitionId);

    ProductAttribute definition =
        attributeRepository
            .findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));

    // Check for duplicate value under same definition
    if (valueRepository.existsByProductAttributeIdAndValueIgnoreCase(definitionId, request.getValue())) {
      throw new DuplicateResourceException("AttributeValue", "value", request.getValue());
    }

    ProductAttributeValue entity = attributeMapper.toValueEntity(request);
    entity.setProductAttribute(definition);
    entity.setIsActive(true);

    // Set display order (append to end)
    int maxOrder = valueRepository.findMaxDisplayOrderByProductAttributeId(definitionId);
    if (entity.getDisplayOrder() == null || entity.getDisplayOrder() == 0) {
      entity.setDisplayOrder(maxOrder + 1);
    }

    ProductAttributeValue saved = valueRepository.save(entity);
    log.info(
        "Attribute value created with ID: {} for definition: {}",
        saved.getId(),
        definition.getName());

    return attributeMapper.toValueResponse(saved);
  }

  @Override
  @Transactional
  public ProductAttributeValueResponse updateValue(
      Long valueId, ProductAttributeValueRequest request) {
    log.info("Updating attribute value with ID: {}", valueId);

    ProductAttributeValue existing =
        valueRepository
            .findById(valueId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", valueId));

    // Check for duplicate value under same definition (excluding self)
    if (request.getValue() != null
        && !request.getValue().equals(existing.getValue())
        && valueRepository.existsByProductAttributeIdAndValueIgnoreCase(
            existing.getProductAttribute().getId(), request.getValue())) {
      throw new DuplicateResourceException("AttributeValue", "value", request.getValue());
    }

    attributeMapper.updateValueEntity(existing, request);

    ProductAttributeValue updated = valueRepository.save(existing);
    log.info("Attribute value updated: {}", updated.getId());

    return attributeMapper.toValueResponse(updated);
  }

  @Override
  @Transactional
  public void deactivateValue(Long valueId) {
    log.info("Deactivating attribute value with ID: {}", valueId);

    ProductAttributeValue value =
        valueRepository
            .findById(valueId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", valueId));
    value.setIsActive(false);
    valueRepository.save(value);

    log.info("Attribute value deactivated: {}", valueId);
  }

  @Override
  @Transactional
  public void activateValue(Long valueId) {
    log.info("Activating attribute value with ID: {}", valueId);

    ProductAttributeValue value =
        valueRepository
            .findById(valueId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", valueId));
    value.setIsActive(true);
    valueRepository.save(value);

    log.info("Attribute value activated: {}", valueId);
  }

  @Override
  @Transactional
  public void deleteValue(Long valueId) {
    log.info("Permanently deleting attribute value with ID: {}", valueId);

    ProductAttributeValue value =
        valueRepository
            .findById(valueId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", valueId));

    // Check if this value is used in any variants
    if (value.getVariants() != null && !value.getVariants().isEmpty()) {
      log.warn(
          "Attribute value is used in {} variants. Cannot delete.", value.getVariants().size());
      throw new IllegalStateException(
          "Cannot delete attribute value that is used in product variants");
    }

    valueRepository.delete(value);
    log.info("Attribute value deleted: {}", valueId);
  }

  @Override
  @Transactional
  public void reorderValues(Long definitionId, List<Long> orderedValueIds) {
    log.info("Reordering values for definition ID: {}", definitionId);

    // Verify definition exists
    if (!attributeRepository.existsById(definitionId)) {
      throw new ResourceNotFoundException("AttributeDefinition", definitionId);
    }

    for (int i = 0; i < orderedValueIds.size(); i++) {
      Long valueId = orderedValueIds.get(i);
      ProductAttributeValue value =
          valueRepository
              .findById(valueId)
              .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", valueId));

      // Ensure value belongs to the specified definition
      if (!value.getProductAttribute().getId().equals(definitionId)) {
        throw new IllegalArgumentException(
            "Value " + valueId + " does not belong to definition " + definitionId);
      }

      value.setDisplayOrder(i + 1);
      valueRepository.save(value);
    }

    log.info("Values reordered for definition: {}", definitionId);
  }
}
