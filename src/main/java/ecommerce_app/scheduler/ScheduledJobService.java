package ecommerce_app.scheduler;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.OrderItem;
import ecommerce_app.entity.Payment;
import ecommerce_app.entity.ProductVariant;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.repository.PaymentRepository;
import ecommerce_app.repository.ProductVariantRepository;
import ecommerce_app.service.StockManagementService;
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
  private final StockManagementService stockManagementService;
  private final ProductVariantRepository variantRepository;

  @Scheduled(cron = "0 */30 * * * *") // Run every 30 minutes
  @Transactional
  public void expireCashInShopOrders() {
    log.info("Running scheduled job: expireCashInShopOrders");

    List<Payment> expiredPayments = paymentRepository.findByGatewayAndStatusAndExpiredAtBefore(
            PaymentGateway.CASH_IN_SHOP, PaymentStatus.PENDING, LocalDateTime.now());

    int expiredCount = 0;
    for (Payment payment : expiredPayments) {
      Order order = payment.getOrder();

      // Only expire if order is still in valid statuses
      if (order.getOrderStatus() == OrderStatus.READY_FOR_PICKUP
              || order.getOrderStatus() == OrderStatus.PENDING) {

        // Cancel the order
        order.setOrderStatus(OrderStatus.CANCELLED);
        payment.setStatus(PaymentStatus.FAILED);
        orderRepository.save(order);
        paymentRepository.save(payment);

        // Restore stock for each item
        restoreStockForOrder(order);

        expiredCount++;
        log.info("Expired Cash-in-Shop order #{} (payment #{})",
                order.getOrderNumber(), payment.getId());

        // Send notification
        sendExpiryNotification(order);
      }
    }

    if (expiredCount > 0) {
      log.info("Expired {} Cash-in-Shop orders", expiredCount);
    }
  }

  /**
   * Restore stock for all items in an expired/cancelled order.
   * Uses variant-based stock management.
   */
  private void restoreStockForOrder(Order order) {
    for (OrderItem item : order.getOrderItems()) {
      try {
        // Determine which variant to restore stock to
        Long variantId = resolveVariantId(item);

        if (variantId != null) {
          stockManagementService.adjustStock(
                  StockAdjustmentRequest.builder()
                          .productId(item.getProduct().getId())
                          .variantId(variantId)
                          .movementType(StockMovementType.RETURN)
                          .quantity(item.getQuantity())
                          .referenceType("ORDER_CANCELLED")
                          .referenceId(order.getId())
                          .note("Stock restored from expired order #" + order.getOrderNumber())
                          .build(),
                  null // System action, no specific user
          );

          log.info("Restored {} units of variant {} from expired order #{}",
                  item.getQuantity(), variantId, order.getOrderNumber());
        } else {
          log.error("Cannot restore stock: No variant found for order item {}", item.getId());
        }

      } catch (Exception e) {
        log.error("Failed to restore stock for order item {}: {}",
                item.getId(), e.getMessage(), e);
        // Continue with other items even if one fails
      }
    }
  }

  /**
   * Resolve variant ID from order item.
   * If the order item has a variant, use it.
   * Otherwise, find the default variant for the product.
   */
  private Long resolveVariantId(OrderItem item) {
    // If order item has variant reference
    if (item.getVariant() != null) {
      return item.getVariant().getId();
    }

    // Fall back to default variant for the product
    return variantRepository.findByProductIdAndIsDefaultTrue(item.getProduct().getId())
            .map(ProductVariant::getId)
            .orElse(null);
  }

  private void sendExpiryNotification(Order order) {
    // Implement your notification logic here
    // e.g., push notification, email, SMS
    log.info("Sending expiry notification for order #{}", order.getOrderNumber());

    // Example:
    // notificationService.sendOrderExpiredNotification(order.getUser(), order);
  }
}