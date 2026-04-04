package ecommerce_app.service.strategy;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.dto.request.InitiatePaymentRequest;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.Payment;
import ecommerce_app.service.strategy.PaymentGatewayStrategy;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Cash on Delivery strategy. Payment is collected by delivery person upon delivery. Order is
 * confirmed immediately — payment stays PENDING until delivered.
 */
@Slf4j
@Component
public class CodPaymentStrategy implements PaymentGatewayStrategy {

  @Override
  public PaymentGateway getGateway() {
    return PaymentGateway.COD;
  }

  @Override
  public Payment initiate(Order order, InitiatePaymentRequest request) {
    log.info("Initiating COD payment for order #{}", order.getOrderNumber());

    // COD: payment record created as PENDING
    // Will be marked PAID by staff when cash is collected at delivery
    return Payment.builder()
        .order(order)
        .gateway(PaymentGateway.COD)
        .amount(order.getTotalAmount())
        .currency("USD")
        .status(PaymentStatus.PENDING)
        .gatewayReference("COD-" + order.getOrderNumber())
        .expiredAt(null) // no expiry for COD
        .build();
  }

  @Override
  public void syncStatus(Payment payment) {
    // COD has no external gateway to poll.
    // Status is updated manually by staff when cash is collected.
    log.debug("COD payment #{} — no sync needed, status updated manually", payment.getId());
  }
}
