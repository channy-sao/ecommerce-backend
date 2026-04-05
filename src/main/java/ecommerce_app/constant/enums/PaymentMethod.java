package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT,
    QR_CODE,
    COD,          // Cash on Delivery
    CASH_IN_SHOP  // Customer pays at physical store
}
