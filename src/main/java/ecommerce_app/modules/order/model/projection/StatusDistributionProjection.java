package ecommerce_app.modules.order.model.projection;

import ecommerce_app.constant.enums.OrderStatus;

public interface StatusDistributionProjection {
  OrderStatus getStatus();

  Long getCount();
}
