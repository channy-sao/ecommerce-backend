package ecommerce_app.service.impl;

import ecommerce_app.dto.request.ProductAttributeDefinitionRequest;
import ecommerce_app.dto.request.ProductAttributeValueRequest;
import ecommerce_app.dto.response.ProductAttributeDefinitionResponse;
import ecommerce_app.dto.response.ProductAttributeValueResponse;
import ecommerce_app.entity.ProductAttributeDefinition;
import ecommerce_app.entity.ProductAttributeValue;
import ecommerce_app.exception.DuplicateResourceException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.ProductAttributeMapper;
import ecommerce_app.repository.ProductAttributeDefinitionRepository;
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

  private final ProductAttributeDefinitionRepository definitionRepository;
  private final ProductAttributeValueRepository valueRepository;
  private final ProductAttributeMapper attributeMapper;

  // ==================== Attribute Definitions ====================

  @Override
  public List<ProductAttributeDefinitionResponse> getAllDefinitions() {
    log.debug("Fetching all attribute definitions");
    return definitionRepository.findAll().stream().map(attributeMapper::toResponse).toList();
  }

  @Override
  public Page<ProductAttributeDefinitionResponse> getDefinitions(Pageable pageable) {
    log.debug("Fetching attribute definitions with pagination: {}", pageable);
    return definitionRepository.findAll(pageable).map(attributeMapper::toResponse);
  }

  @Override
  public List<ProductAttributeDefinitionResponse> getActiveDefinitions() {
    log.debug("Fetching active attribute definitions");
    return definitionRepository.findByIsActiveTrue().stream()
        .map(attributeMapper::toResponse)
        .toList();
  }

  @Override
  public ProductAttributeDefinitionResponse getDefinitionById(Long definitionId) {
    log.debug("Fetching attribute definition by ID: {}", definitionId);
    return definitionRepository
        .findById(definitionId)
        .map(attributeMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));
  }

  @Override
  @Transactional
  public ProductAttributeDefinitionResponse createDefinition(
      ProductAttributeDefinitionRequest request) {
    log.info("Creating new attribute definition: {}", request.getName());

    // Check for duplicate name
    if (definitionRepository.existsByNameIgnoreCase(request.getName())) {
      throw new DuplicateResourceException("AttributeDefinition", "name", request.getName());
    }

    ProductAttributeDefinition entity = attributeMapper.toEntity(request);
    entity.setIsActive(true);

    ProductAttributeDefinition saved = definitionRepository.save(entity);
    log.info("Attribute definition created with ID: {}", saved.getId());

    return attributeMapper.toResponse(saved);
  }

  @Override
  @Transactional
  public ProductAttributeDefinitionResponse updateDefinition(
      Long definitionId, ProductAttributeDefinitionRequest request) {
    log.info("Updating attribute definition with ID: {}", definitionId);

    ProductAttributeDefinition existing =
        definitionRepository
            .findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));

    // Check for duplicate name (excluding self)
    if (request.getName() != null
        && !request.getName().equals(existing.getName())
        && definitionRepository.existsByNameIgnoreCase(request.getName())) {
      throw new DuplicateResourceException("AttributeDefinition", "name", request.getName());
    }

    attributeMapper.updateEntity(existing, request);

    ProductAttributeDefinition updated = definitionRepository.save(existing);
    log.info("Attribute definition updated: {}", updated.getId());

    return attributeMapper.toResponse(updated);
  }

  @Override
  @Transactional
  public void deactivateDefinition(Long definitionId) {
    log.info("Deactivating attribute definition with ID: {}", definitionId);

    ProductAttributeDefinition definition =
        definitionRepository
            .findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));
    definition.setIsActive(false);
    definitionRepository.save(definition);

    log.info("Attribute definition deactivated: {}", definitionId);
  }

  @Override
  @Transactional
  public void activateDefinition(Long definitionId) {
    log.info("Activating attribute definition with ID: {}", definitionId);

    ProductAttributeDefinition definition =
        definitionRepository
            .findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));
    definition.setIsActive(true);
    definitionRepository.save(definition);

    log.info("Attribute definition activated: {}", definitionId);
  }

  @Override
  @Transactional
  public void deleteDefinition(Long definitionId) {
    log.info("Permanently deleting attribute definition with ID: {}", definitionId);

    ProductAttributeDefinition definition =
        definitionRepository
            .findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));

    long valueCount = valueRepository.countByDefinitionId(definitionId);
    if (valueCount > 0) {
      log.warn(
          "Deleting definition with {} associated values. Values will also be deleted due to cascade.",
          valueCount);
    }

    definitionRepository.delete(definition);
    log.info("Attribute definition deleted: {}", definitionId);
  }

  // ==================== Attribute Values ====================

  @Override
  public List<ProductAttributeValueResponse> getValuesByDefinition(Long definitionId) {
    log.debug("Fetching all values for definition ID: {}", definitionId);

    // Verify definition exists
    if (!definitionRepository.existsById(definitionId)) {
      throw new ResourceNotFoundException("AttributeDefinition", definitionId);
    }

    return valueRepository.findByDefinitionIdOrderByDisplayOrderAsc(definitionId).stream()
        .map(attributeMapper::toValueResponse)
        .toList();
  }

  @Override
  public List<ProductAttributeValueResponse> getActiveValuesByDefinition(Long definitionId) {
    log.debug("Fetching active values for definition ID: {}", definitionId);

    // Verify definition exists
    if (!definitionRepository.existsById(definitionId)) {
      throw new ResourceNotFoundException("AttributeDefinition", definitionId);
    }

    return valueRepository
        .findByDefinitionIdAndIsActiveTrueOrderByDisplayOrderAsc(definitionId)
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

    ProductAttributeDefinition definition =
        definitionRepository
            .findById(definitionId)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", definitionId));

    // Check for duplicate value under same definition
    if (valueRepository.existsByDefinitionIdAndValueIgnoreCase(definitionId, request.getValue())) {
      throw new DuplicateResourceException("AttributeValue", "value", request.getValue());
    }

    ProductAttributeValue entity = attributeMapper.toValueEntity(request);
    entity.setDefinition(definition);
    entity.setIsActive(true);

    // Set display order (append to end)
    int maxOrder = valueRepository.findMaxDisplayOrderByDefinitionId(definitionId);
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
        && valueRepository.existsByDefinitionIdAndValueIgnoreCase(
            existing.getDefinition().getId(), request.getValue())) {
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
    if (!definitionRepository.existsById(definitionId)) {
      throw new ResourceNotFoundException("AttributeDefinition", definitionId);
    }

    for (int i = 0; i < orderedValueIds.size(); i++) {
      Long valueId = orderedValueIds.get(i);
      ProductAttributeValue value =
          valueRepository
              .findById(valueId)
              .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", valueId));

      // Ensure value belongs to the specified definition
      if (!value.getDefinition().getId().equals(definitionId)) {
        throw new IllegalArgumentException(
            "Value " + valueId + " does not belong to definition " + definitionId);
      }

      value.setDisplayOrder(i + 1);
      valueRepository.save(value);
    }

    log.info("Values reordered for definition: {}", definitionId);
  }
}
