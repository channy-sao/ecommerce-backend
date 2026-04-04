package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum PaymentGateway {
  BAKONG, // KHQR — not yet implemented
  COD, // Cash on Delivery
  CASH_IN_SHOP // Customer pays at physical store
}
