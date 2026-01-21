package ecommerce_app.modules.order.model.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RevenueTrendProjection {
  LocalDate getOrderDate();

  BigDecimal getRevenue();

  Long getOrders();
}
