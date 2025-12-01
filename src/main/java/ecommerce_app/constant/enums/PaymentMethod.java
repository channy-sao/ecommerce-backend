package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT,
    QR_CODE,
    // other here
}
