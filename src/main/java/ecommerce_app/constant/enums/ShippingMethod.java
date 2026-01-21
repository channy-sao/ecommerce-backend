package ecommerce_app.constant.enums;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShippingMethod {
  STANDARD("Standard Shipping", 1.5),
  FREE("Free Shipping", 0),
  PICKUP("Store Pickup", 0),
  EXPRESS("Express Shipping", 1);
  private final String displayName;
  private final double defaultCost;

  public BigDecimal getDefaultCostAsBigDecimal() {
    return BigDecimal.valueOf(defaultCost);
  }

  public boolean isFree() {
    return this == FREE || this == PICKUP;
  }
}
