package ecommerce_app.modules.order.specification;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.modules.order.model.entity.Order;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSpecification {

  public static Specification<Order> filter(
      OrderStatus orderStatus, PaymentStatus paymentStatus, LocalDate fromDate, LocalDate toDate) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (orderStatus != null) {
        predicates.add(cb.equal(root.get("orderStatus"), orderStatus));
      }

      if (paymentStatus != null) {
        predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus));
      }

      if (fromDate != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), fromDate.atStartOfDay()));
      }

      if (toDate != null) {
        // Use end of day OR next day's start
        predicates.add(cb.lessThan(root.get("orderDate"), toDate.plusDays(1).atStartOfDay()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
