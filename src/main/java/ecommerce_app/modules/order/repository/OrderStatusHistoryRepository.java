package ecommerce_app.modules.order.repository;

import ecommerce_app.modules.order.model.entity.OrderStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
  List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
