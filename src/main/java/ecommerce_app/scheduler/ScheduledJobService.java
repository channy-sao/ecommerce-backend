// Create new file: ScheduledJobService.java
package ecommerce_app.scheduler;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.Payment;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.repository.PaymentRepository;
import ecommerce_app.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledJobService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final StockRepository stockRepository;

  @Scheduled(cron = "0 */30 * * * *") // Run every 30 minutes
  @Transactional
  public void expireCashInShopOrders() {
    log.info("Running scheduled job: expireCashInShopOrders");

    List<Payment> expiredPayments =
        paymentRepository.findByGatewayAndStatusAndExpiredAtBefore(
            PaymentGateway.CASH_IN_SHOP, PaymentStatus.PENDING, LocalDateTime.now());

    int expiredCount = 0;
    for (Payment payment : expiredPayments) {
      Order order = payment.getOrder();

      // Only expire if order is still in READY_FOR_PICKUP or PENDING status
      if (order.getOrderStatus() == OrderStatus.READY_FOR_PICKUP
          || order.getOrderStatus() == OrderStatus.PENDING) {

        // Cancel the order
        order.setOrderStatus(OrderStatus.CANCELLED);
        payment.setStatus(PaymentStatus.FAILED);

        orderRepository.save(order);
        paymentRepository.save(payment);

        // Restore stock
        restoreStock(order);

        expiredCount++;
        log.info(
            "Expired Cash-in-Shop order #{} (payment #{})",
            order.getOrderNumber(),
            payment.getId());

        // Send notification to customer
        sendExpiryNotification(order);
      }
    }

    if (expiredCount > 0) {
      log.info("Expired {} Cash-in-Shop orders", expiredCount);
    }
  }

  private void restoreStock(Order order) {
    order
        .getOrderItems()
        .forEach(
            item -> {
              stockRepository
                  .findByProductId(item.getProduct().getId())
                  .ifPresent(
                      stock -> {
                        stock.setQuantity(stock.getQuantity() + item.getQuantity());
                        stockRepository.save(stock);
                        log.info(
                            "Restored {} units of product #{}",
                            item.getQuantity(),
                            item.getProduct().getId());
                      });
            });
  }

  private void sendExpiryNotification(Order order) {
    // Implement notification logic here
    log.info("Sending expiry notification for order #{}", order.getOrderNumber());
  }
}
