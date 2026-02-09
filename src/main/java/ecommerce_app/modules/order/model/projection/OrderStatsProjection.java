package ecommerce_app.modules.order.model.projection;

import java.math.BigDecimal;

public interface OrderStatsProjection {
  BigDecimal getCurrentRevenue();

  Long getCurrentOrders();

  Long getCurrentPending();

  Long getCurrentProcessing();

  Long getCurrentShipped();

  Long getCurrentDelivered();

  Long getCurrentCompleted();

  Long getCurrentCancelled();

  BigDecimal getPreviousRevenue();

  Long getPreviousOrders();

  Long getPreviousPending();

  Long getPreviousCompleted();

  Long getPreviousCancelled();
}
