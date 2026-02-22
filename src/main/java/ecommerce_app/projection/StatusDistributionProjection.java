package ecommerce_app.projection;

import ecommerce_app.constant.enums.OrderStatus;

public interface StatusDistributionProjection {
  OrderStatus getStatus();

  Long getCount();
}
