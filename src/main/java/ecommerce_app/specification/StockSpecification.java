package ecommerce_app.specification;

import ecommerce_app.constant.enums.StockStatus;
import ecommerce_app.entity.Stock;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockSpecification {
  public static Specification<Stock> filter(String filter) {
    return (root, query, cb) -> {
      if (filter == null || filter.trim().isEmpty()) {
        return cb.conjunction();
      }

      String likeFilter = "%" + filter.trim() + "%";

      List<Predicate> predicates = new ArrayList<>();

      predicates.add(cb.like(cb.lower(root.get("product").get("name")), likeFilter.toLowerCase()));

      predicates.add(
          cb.like(
              cb.lower(root.get("product").get("category").get("name")), likeFilter.toLowerCase()));

      return cb.or(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<Stock> hasStockStatus(StockStatus status) {
    return (root, query, cb) -> {
      if (status == null) {
        return cb.conjunction();
      }

      Path<Integer> quantity = root.get("quantity");

      return switch (status) {
        case OUT_OF_STOCK ->
            // Treat quantity null or <= 0 as out of stock
            cb.or(cb.isNull(quantity), cb.lessThanOrEqualTo(quantity, 0));
        case LOW_STOCK ->
            // quantity > 0 AND quantity <= threshold
            cb.and(
                cb.greaterThan(quantity, 0),
                cb.lessThanOrEqualTo(quantity, 10) // threshold can be a constant
                );
        case IN_STOCK -> cb.greaterThan(quantity, 10); // same threshold

        default -> cb.conjunction();
      };
    };
  }
}
