package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum PaymentGateway {
  BAKONG, // KHQR — not yet implemented
  COD, // Cash on Delivery
  CASH_IN_SHOP; // Customer pays at physical store

  /**
   * Resolve PaymentGateway from PaymentMethod selected at checkout. Called in PaymentServiceImpl to
   * auto-resolve strategy.
   */
  public static PaymentGateway fromPaymentMethod(PaymentMethod method) {
    return switch (method) {
      case COD -> COD;
      case CASH_IN_SHOP -> CASH_IN_SHOP;
      case QR_CODE -> BAKONG;
      default ->
          throw new IllegalArgumentException("No payment gateway mapped for method: " + method);
    };
  }
}
