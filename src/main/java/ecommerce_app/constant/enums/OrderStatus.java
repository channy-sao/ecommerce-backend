package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum OrderStatus {
CONFIRMED,
  COMPLETED,
  CANCELLED,
  PENDING,
  PAID,
  PROCESSING,
  SHIPPED,
  DELIVERED,
  READY_FOR_PICKUP,
  REFUNDED;
}
