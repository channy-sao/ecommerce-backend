// ecommerce_app/util/WarrantyUtil.java
package ecommerce_app.util;

import ecommerce_app.constant.enums.WarrantyType;
import ecommerce_app.constant.enums.WarrantyUnit;

public class WarrantyUtil {

  private WarrantyUtil() {}

  public static String buildLabel(WarrantyType type, Integer duration, WarrantyUnit unit) {
    if (type == null || type == WarrantyType.NONE) return "No Warranty";

    String durationStr =
        (duration != null && unit != null) ? duration + " " + capitalize(unit.name()) : "";

    String typeStr =
        switch (type) {
          case MANUFACTURER -> "Manufacturer Warranty";
          case SELLER -> "Seller Warranty";
          default -> "Warranty";
        };

    return durationStr.isBlank() ? typeStr : durationStr + " " + typeStr;
    // → "12 Months Manufacturer Warranty"
    // → "1 Year Seller Warranty"
    // → "No Warranty"
  }

  private static String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.charAt(0) + s.substring(1).toLowerCase();
    // "MONTHS" → "Months"
  }
}
