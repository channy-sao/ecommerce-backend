package ecommerce_app.modules.payment.strategy;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.payment.model.dto.InitiatePaymentRequest;
import ecommerce_app.modules.payment.model.entity.Payment;

/**
 * Strategy interface — each gateway (Bakong, Stripe) implements this. PaymentService resolves the
 * correct implementation at runtime.
 */
public interface PaymentGatewayStrategy {

  /** Which gateway does this strategy handle? */
  PaymentGateway getGateway();

  /**
   * Create a payment record and call the upstream gateway. Returns a Payment (not yet persisted —
   * PaymentService saves it).
   */
  Payment initiate(Order order, InitiatePaymentRequest request);

  /**
   * Poll the gateway to get current status. Updates the Payment in-place; PaymentService persists
   * it.
   */
  void syncStatus(Payment payment);
}
