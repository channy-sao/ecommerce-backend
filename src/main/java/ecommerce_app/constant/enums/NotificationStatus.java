package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum NotificationStatus {
  PENDING,
  SENT,
  DELIVERED,
  FAILED,
  READ,
  UNREAD
}
