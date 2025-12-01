package ecommerce_app.modules.user.specification;

import ecommerce_app.modules.user.model.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSpecification {
  /**
   * Builds a specification that matches users by email, phone, first name, or last name.
   *
   * @param filter the search string to match against user fields
   * @return a specification for filtering users
   */
  public static Specification<User> byFilter(String filter) {
    return (root, query, criteriaBuilder) -> {
      if (filter == null || filter.isBlank()) {
        return criteriaBuilder.conjunction(); // no filtering
      }

      String likeFilter = "%" + filter.toLowerCase() + "%";
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeFilter));
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), likeFilter));
      predicates.add(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likeFilter));
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likeFilter));

      return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
    };
  }
}
