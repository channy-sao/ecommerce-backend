package ecommerce_app.specification;

import ecommerce_app.entity.Role;
import ecommerce_app.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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

  /**
   * Filters users by role ID.
   *
   * @param roleId the role ID to filter by (nullable)
   * @return a specification that filters by role ID, or a no-op predicate if roleId is null
   */
  public static Specification<User> byRoleId(Long roleId) {
    return (root, query, cb) -> {
      if (roleId == null) {
        return cb.conjunction();
      }

      Join<User, Role> roleJoin = root.join("roles");

      return cb.equal(roleJoin.get("id"), roleId);
    };
  }

  /**
   * Filters users by role name (case-insensitive, partial match).
   *
   * @param roleName the role name to search (nullable)
   * @return a specification that filters by role name using LIKE, or a no-op predicate if roleName
   *     is null or blank
   */
  public static Specification<User> byRoleName(String roleName) {
    return (root, query, cb) -> {
      if (roleName == null || roleName.isBlank()) {
        return cb.conjunction();
      }
      Join<Object, Object> roleJoin = root.join("role", JoinType.LEFT);
      return cb.like(cb.lower(roleJoin.get("name")), "%" + roleName.toLowerCase() + "%");
    };
  }

  /**
   * Filters users by status.
   *
   * <p>Accepted values:
   *
   * <ul>
   *   <li>"active" → filters users where isActive = true
   *   <li>"inactive" → filters users where isActive = false
   * </ul>
   *
   * @param status the status string (nullable)
   * @return a specification filtering by active flag, or a no-op predicate if status is null or
   *     invalid
   */
  public static Specification<User> byStatus(String status) {
    return (root, query, cb) -> {
      if (status == null || status.isBlank()) {
        return cb.conjunction();
      }

      String normalized = status.trim().toLowerCase();

      if ("active".equals(normalized)) {
        return cb.isTrue(root.get("isActive"));
      } else if ("inactive".equals(normalized)) {
        return cb.isFalse(root.get("isActive"));
      }

      // If invalid status value → ignore filter
      return cb.conjunction();
    };
  }
}
