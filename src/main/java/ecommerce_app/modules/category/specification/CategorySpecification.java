package ecommerce_app.modules.category.specification;

import ecommerce_app.modules.category.model.entity.Category;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategorySpecification {
  public static Specification<Category> filter(String filter) {
    return (root, query, criteriaBuilder) -> {
      if (filter == null || filter.isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.or(
          criteriaBuilder.like(
              criteriaBuilder.lower(root.get("name")), "%" + filter.toLowerCase() + "%"),
          criteriaBuilder.like(
              criteriaBuilder.lower(root.get("description")), "%" + filter.toLowerCase() + "%"));
    };
  }
}
