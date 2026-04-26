package ecommerce_app.mapper;

import ecommerce_app.dto.request.ProductAttributeDefinitionRequest;
import ecommerce_app.dto.request.ProductAttributeValueRequest;
import ecommerce_app.dto.response.AuditUserDto;
import ecommerce_app.dto.response.ProductAttributeDefinitionResponse;
import ecommerce_app.dto.response.ProductAttributeValueResponse;
import ecommerce_app.entity.ProductAttributeDefinition;
import ecommerce_app.entity.ProductAttributeValue;
import ecommerce_app.util.AuditUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Mapper for converting between Product Attribute entities and DTOs */
@Component
@RequiredArgsConstructor
public class ProductAttributeMapper {
  private final AuditUserResolver resolver;

  // ==================== Definition Mappings ====================

  /** Convert entity to response DTO */
  public ProductAttributeDefinitionResponse toResponse(ProductAttributeDefinition entity) {
    if (entity == null) {
      return null;
    }
    Map<Long, AuditUserDto> auditUserDtoMap =
        resolver.resolve(List.of(entity.getCreatedBy(), entity.getUpdatedBy()));
    return ProductAttributeDefinitionResponse.builder()
        .id(entity.getId())
        .name(entity.getName())
        .displayName(entity.getDisplayName())
        .isActive(entity.getIsActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(auditUserDtoMap.get(entity.getCreatedBy()))
        .updatedBy(auditUserDtoMap.get(entity.getUpdatedBy()))
        .values(toValueResponseList(entity.getValues()))
        .build();
  }

  /** Convert request DTO to entity */
  public ProductAttributeDefinition toEntity(ProductAttributeDefinitionRequest request) {
    if (request == null) {
      return null;
    }

    return ProductAttributeDefinition.builder()
        .id(request.getId())
        .name(request.getName())
        .displayName(request.getDisplayName())
        .isActive(request.getIsActive())
        .build();
  }

  /** Update existing entity from request DTO */
  public void updateEntity(
      ProductAttributeDefinition entity, ProductAttributeDefinitionRequest request) {
    if (request == null || entity == null) {
      return;
    }

    if (request.getName() != null) {
      entity.setName(request.getName());
    }
    if (request.getDisplayName() != null) {
      entity.setDisplayName(request.getDisplayName());
    }
    if (request.getIsActive() != null) {
      entity.setIsActive(request.getIsActive());
    }
  }

  /** Convert list of entities to list of response DTOs */
  public List<ProductAttributeDefinitionResponse> toResponseList(
      List<ProductAttributeDefinition> entities) {
    if (entities == null) {
      return Collections.emptyList();
    }
    return entities.stream().map(this::toResponse).collect(Collectors.toList());
  }

  // ==================== Value Mappings ====================

  /** Convert entity to response DTO */
  public ProductAttributeValueResponse toValueResponse(ProductAttributeValue entity) {
    if (entity == null) {
      return null;
    }

    return ProductAttributeValueResponse.builder()
        .id(entity.getId())
        .attributeDefinitionId(
            entity.getDefinition() != null ? entity.getDefinition().getId() : null)
        .attributeDefinitionName(
            entity.getDefinition() != null ? entity.getDefinition().getName() : null)
        .value(entity.getValue())
        .displayOrder(entity.getDisplayOrder())
        .isActive(entity.getIsActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  /** Convert request DTO to entity */
  public ProductAttributeValue toValueEntity(ProductAttributeValueRequest request) {
    if (request == null) {
      return null;
    }

    return ProductAttributeValue.builder()
        .id(request.getId())
        .value(request.getValue())
        .displayOrder(request.getDisplayOrder())
        .isActive(request.getIsActive())
        .build();
  }

  /** Update existing entity from request DTO */
  public void updateValueEntity(
      ProductAttributeValue entity, ProductAttributeValueRequest request) {
    if (request == null || entity == null) {
      return;
    }

    if (request.getValue() != null) {
      entity.setValue(request.getValue());
    }
    if (request.getDisplayOrder() != null) {
      entity.setDisplayOrder(request.getDisplayOrder());
    }
    if (request.getIsActive() != null) {
      entity.setIsActive(request.getIsActive());
    }
  }

  /** Convert list of entities to list of response DTOs */
  public List<ProductAttributeValueResponse> toValueResponseList(
      List<ProductAttributeValue> entities) {
    if (entities == null) {
      return Collections.emptyList();
    }
    return entities.stream().map(this::toValueResponse).collect(Collectors.toList());
  }

  // ==================== Batch Operations ====================

  /** Convert request DTOs to entities with definition association */
  public List<ProductAttributeValue> toValueEntities(
      List<ProductAttributeValueRequest> requests, ProductAttributeDefinition definition) {

    if (requests == null) {
      return Collections.emptyList();
    }

    return requests.stream()
        .map(
            request -> {
              ProductAttributeValue value = toValueEntity(request);
              value.setDefinition(definition);
              return value;
            })
        .collect(Collectors.toList());
  }
}
