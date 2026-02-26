package ecommerce_app.specification;

import ecommerce_app.entity.Promotion;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PromotionSpecification {
  public static Specification<Promotion> filter(String query, Boolean active, String discountType) {
    return (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Search by name or code
      if (query != null && !query.isBlank()) {
        String pattern = "%" + query.toLowerCase() + "%";
        Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
        Predicate codeLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), pattern);
        predicates.add(criteriaBuilder.or(nameLike, codeLike));
      }

      // Filter by active status
      if (active != null) {
        predicates.add(criteriaBuilder.equal(root.get("active"), active));
      }

      // Filter by discount type
      if (discountType != null && !discountType.isBlank()) {
        predicates.add(
            criteriaBuilder.equal(root.get("discountType").as(String.class), discountType));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
