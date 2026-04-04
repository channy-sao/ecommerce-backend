package ecommerce_app.service.strategy;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.dto.request.InitiatePaymentRequest;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.Payment;
import ecommerce_app.service.strategy.PaymentGatewayStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Bakong / KHQR payment strategy. NOT YET IMPLEMENTED — will integrate with Bakong Open API later.
 */
@Slf4j
@Component
public class BakongPaymentStrategy implements PaymentGatewayStrategy {

  @Override
  public PaymentGateway getGateway() {
    return PaymentGateway.BAKONG;
  }

  @Override
  public Payment initiate(Order order, InitiatePaymentRequest request) {
    log.warn("KHQR/Bakong payment is not yet implemented for order #{}", order.getOrderNumber());
    throw new UnsupportedOperationException(
        "KHQR / Bakong payment is not yet available. Please choose COD or Cash in Shop.");
  }

  @Override
  public void syncStatus(Payment payment) {
    throw new UnsupportedOperationException("KHQR / Bakong payment is not yet available.");
  }
}
