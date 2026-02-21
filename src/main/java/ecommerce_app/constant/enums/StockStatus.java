package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum StockStatus {
  IN_STOCK("In Stock"),
  LOW_STOCK("Low Stock"),
  OUT_OF_STOCK("Out of Stock");

  private final String title;
}
