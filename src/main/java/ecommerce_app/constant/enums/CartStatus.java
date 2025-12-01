package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CartStatus {
  ACTIVE("active"),
  INACTIVE("inactive"),
  ORDERED("ordered"),
  COMPLETED("completed"),
  CANCELED("canceled"),
  CHECKED_OUT("checked-out");

  private final String status;
}
