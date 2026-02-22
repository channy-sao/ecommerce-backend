package ecommerce_app.mapper;

import ecommerce_app.dto.response.SimpleCategoryResponse;
import ecommerce_app.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

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
}
