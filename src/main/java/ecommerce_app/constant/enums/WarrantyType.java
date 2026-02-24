// ecommerce_app/constant/enums/WarrantyType.java
package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum WarrantyType {
  MANUFACTURER, // brand covers it
  SELLER, // your store covers it
  NONE // no warranty
}
