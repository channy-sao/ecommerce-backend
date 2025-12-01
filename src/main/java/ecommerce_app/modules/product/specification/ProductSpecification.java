package ecommerce_app.modules.product.specification;

import ecommerce_app.modules.product.model.entity.Product;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductSpecification {
  public static Specification<Product> withCategory(Long categoryId) {
    return (root, query, criteriaBuilder) -> {
      if (categoryId == null) {
        return criteriaBuilder.conjunction(); // return all products
      }
      return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    };
  }

  public static Specification<Product> withCategoryName(String categoryName) {
    return (root, query, criteriaBuilder) -> {
      if (categoryName == null) {
        return criteriaBuilder.conjunction(); // return all products
      }
      return criteriaBuilder.like(
          criteriaBuilder.lower(root.get("category").get("name")),
          "%" + categoryName.toLowerCase() + "%");
    };
  }

  public static Specification<Product> withName(String name) {
    return (root, query, criteriaBuilder) -> {
      if (name == null) {
        return criteriaBuilder.conjunction(); // listing all products
      }
      return criteriaBuilder.like(
          criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    };
  }

  public static Specification<Product> withDescription(String description) {
    return (root, query, criteriaBuilder) -> {
      if (description == null) {
        return criteriaBuilder.conjunction(); // listing all products
      }
      return criteriaBuilder.like(
          criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    };
  }
}
