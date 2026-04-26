package ecommerce_app.mapper;

import ecommerce_app.dto.request.ProductAttributeRequest;
import ecommerce_app.dto.request.ProductAttributeValueRequest;
import ecommerce_app.dto.response.AuditUserDto;
import ecommerce_app.dto.response.ProductAttributeResponse;
import ecommerce_app.dto.response.ProductAttributeValueResponse;
import ecommerce_app.entity.ProductAttribute;
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
  public ProductAttributeResponse toResponse(ProductAttribute entity) {
    if (entity == null) {
      return null;
    }
    Map<Long, AuditUserDto> auditUserDtoMap =
        resolver.resolve(List.of(entity.getCreatedBy(), entity.getUpdatedBy()));
    return ProductAttributeResponse.builder()
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
  public ProductAttribute toEntity(ProductAttributeRequest request) {
    if (request == null) {
      return null;
    }

    return ProductAttribute.builder()
        .id(request.getId())
        .name(request.getName())
        .displayName(request.getDisplayName())
        .isActive(request.getIsActive())
        .build();
  }

  /** Update existing entity from request DTO */
  public void updateEntity(
          ProductAttribute entity, ProductAttributeRequest request) {
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
  public List<ProductAttributeResponse> toResponseList(
      List<ProductAttribute> entities) {
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
            entity.getProductAttribute() != null ? entity.getProductAttribute().getId() : null)
        .attributeDefinitionName(
            entity.getProductAttribute() != null ? entity.getProductAttribute().getName() : null)
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
      List<ProductAttributeValueRequest> requests, ProductAttribute definition) {

    if (requests == null) {
      return Collections.emptyList();
    }

    return requests.stream()
        .map(
            request -> {
              ProductAttributeValue value = toValueEntity(request);
              value.setProductAttribute(definition);
              return value;
            })
        .collect(Collectors.toList());
  }
}
