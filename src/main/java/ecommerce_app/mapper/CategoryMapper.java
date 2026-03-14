package ecommerce_app.mapper;

import ecommerce_app.dto.response.CategoryResponse;
import ecommerce_app.dto.response.SimpleCategoryResponse;
import ecommerce_app.entity.Category;
import ecommerce_app.util.AuditUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CategoryMapper {
  private final AuditUserResolver auditUserResolver;

  public SimpleCategoryResponse toSimpleResponse(Category category) {
    if (category == null) {
      return null;
    }

    return SimpleCategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .icon(category.getIcon())
        .displayOrder(category.getDisplayOrder())
        .description(category.getDescription())
        .build();
  }

  // For minimal response without description
  public SimpleCategoryResponse toMinimalResponse(Category category) {
    if (category == null) {
      return null;
    }

    return SimpleCategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .icon(category.getIcon())
        .displayOrder(category.getDisplayOrder())
        .build();
  }

  public CategoryResponse toResponse(Category category) {
    if (category == null) {
      return null;
    }
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .icon(category.getIcon())
        .displayOrder(category.getDisplayOrder())
        .description(category.getDescription())
        .productCount(Objects.isNull(category.getProducts()) ? 0 : category.getProducts().size())
        .createdAt(category.getCreatedAt())
        .updatedAt(category.getUpdatedAt())
        .build();
  }
}
