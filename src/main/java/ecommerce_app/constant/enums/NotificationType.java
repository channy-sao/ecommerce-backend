package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum NotificationType {
  ORDER_CREATED,
  ORDER_CONFIRMED,
  ORDER_SHIPPED,
  ORDER_DELIVERED,
  ORDER_CANCELLED,
  PRODUCT_STOCK_LOW,
  PRODUCT_AVAILABLE,
  PAYMENT_SUCCESS,
  PAYMENT_FAILED,
  NEW_PROMOTION,
  NEW_COUPON,
  PROMOTION_EXPIRING,   // remind before expires
  COUPON_ASSIGNED,      // user specific coupon assigned
  COUPON_EXPIRING,      // remind before coupon expires
  SYSTEM,
  CUSTOM,
  CHAT,
  ORDER,
  SHIPPING,
  ACCOUNT,
  REVIEW,
  WISHLIST,
  GENERAL
}
