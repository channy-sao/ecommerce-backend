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
 * Cash in Shop strategy. Customer ordered online and will pay in person at the physical store.
 * Order is confirmed immediately — staff marks payment PAID when customer arrives.
 */
@Slf4j
@Component
public class CashInShopPaymentStrategy implements PaymentGatewayStrategy {

  // Orders expire if customer doesn't show up within 24 hours
  private static final int EXPIRY_HOURS = 24;

  @Override
  public PaymentGateway getGateway() {
    return PaymentGateway.CASH_IN_SHOP;
  }

  @Override
  public Payment initiate(Order order, InitiatePaymentRequest request) {
    log.info("Initiating Cash-in-Shop payment for order #{}", order.getOrderNumber());

    return Payment.builder()
        .order(order)
        .gateway(PaymentGateway.CASH_IN_SHOP)
        .amount(order.getTotalAmount())
        .currency("USD")
        .status(PaymentStatus.PENDING)
        .gatewayReference("SHOP-" + order.getOrderNumber())
        .expiredAt(LocalDateTime.now().plusHours(EXPIRY_HOURS))
        .build();
  }

  @Override
  public void syncStatus(Payment payment) {
    // Check if reservation has expired
    if (payment.getExpiredAt() != null
        && LocalDateTime.now().isAfter(payment.getExpiredAt())
        && payment.getStatus() == PaymentStatus.PENDING) {
      log.info("Cash-in-Shop payment #{} expired — marking as FAILED", payment.getId());
      payment.setStatus(PaymentStatus.FAILED);
    }
    // Otherwise no external sync needed — staff marks it paid manually
  }
}
