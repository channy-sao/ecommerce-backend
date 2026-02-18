package ecommerce_app.modules.payment.service;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.order.repository.OrderRepository;
import ecommerce_app.modules.payment.model.dto.InitiatePaymentRequest;
import ecommerce_app.modules.payment.model.dto.InitiatePaymentResponse;
import ecommerce_app.modules.payment.model.dto.PaymentStatusResponse;
import ecommerce_app.modules.payment.model.entity.Payment;
import ecommerce_app.modules.payment.repository.PaymentRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ecommerce_app.modules.payment.strategy.PaymentGatewayStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment orchestration service.
 *
 * <p>Responsibilities: - Resolves the correct PaymentGatewayStrategy by PaymentGateway enum. -
 * Persists Payment entities. - Propagates payment result → Order.paymentStatus / Order.orderStatus.
 * - Exposes polling endpoint logic (getStatus).
 */
@Slf4j
@Service
@Transactional
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;

  /** All gateway strategies injected as a list, then indexed by gateway type */
  private final Map<PaymentGateway, PaymentGatewayStrategy> strategies;

  public PaymentService(
      PaymentRepository paymentRepository,
      OrderRepository orderRepository,
      List<PaymentGatewayStrategy> gatewayStrategies) {
    this.paymentRepository = paymentRepository;
    this.orderRepository = orderRepository;
    this.strategies =
        gatewayStrategies.stream()
            .collect(Collectors.toMap(PaymentGatewayStrategy::getGateway, Function.identity()));
  }

  // ─── 1. Initiate (called immediately after checkout) ─────────────────────
  /**
   * Called by checkout endpoint after order is saved with status PENDING. Delegates to the correct
   * gateway strategy and persists the Payment.
   */
  public InitiatePaymentResponse initiate(InitiatePaymentRequest request, Long userId) {
    log.info("Initiating payment for order #{} via {}", request.getOrderId(), request.getGateway());

    // Load order (must belong to the requesting user)
    Order order =
        orderRepository
            .findByIdAndUserId(request.getOrderId(), userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

    // Guard: do not allow duplicate payment for already-paid orders
    if (order.getPaymentStatus() == PaymentStatus.PAID) {
      throw new IllegalStateException("Order #" + order.getOrderNumber() + " is already paid.");
    }

    // Resolve strategy
    PaymentGatewayStrategy strategy = resolveStrategy(request.getGateway());

    // Let strategy create the payment object
    Payment payment = strategy.initiate(order, request);
    payment.setOrder(order);

    // Persist
    Payment savedPayment = paymentRepository.save(payment);
    log.info("Payment #{} created for order #{}", savedPayment.getId(), order.getOrderNumber());

    // Build response for client
    return buildInitiateResponse(savedPayment);
  }

  // ─── 2. Poll status (client calls every N seconds) ───────────────────────
  @Transactional
  public PaymentStatusResponse getStatus(Long paymentId, Long userId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

    // Security: ensure payment belongs to this user's order
    if (!payment.getOrder().getUser().getId().equals(userId)) {
      throw new SecurityException("Access denied to payment " + paymentId);
    }

    // Only poll gateway if still pending
    if (payment.getStatus() == PaymentStatus.PENDING) {
      PaymentGatewayStrategy strategy = resolveStrategy(payment.getGateway());
      strategy.syncStatus(payment);
      paymentRepository.save(payment);

      // Propagate to order
      if (payment.getStatus() == PaymentStatus.PAID
          || payment.getStatus() == PaymentStatus.FAILED) {
        updateOrderStatus(payment);
      }
    }

    return buildStatusResponse(payment);
  }

  // ─── 3. Handle confirmed payment (called by webhook handlers) ────────────
  /**
   * Finalizes a payment that has been confirmed by gateway webhook. Idempotent — safe to call
   * multiple times.
   */
  @Transactional
  public void confirmPayment(Payment payment) {
    if (payment.getStatus() == PaymentStatus.PAID) {
      log.info("Payment #{} already confirmed — skipping", payment.getId());
      return;
    }
    paymentRepository.save(payment);
    updateOrderStatus(payment);
    log.info("Payment #{} confirmed with status {}", payment.getId(), payment.getStatus());
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  private PaymentGatewayStrategy resolveStrategy(PaymentGateway gateway) {
    PaymentGatewayStrategy strategy = strategies.get(gateway);
    if (strategy == null) {
      throw new IllegalArgumentException("Unsupported payment gateway: " + gateway);
    }
    return strategy;
  }

  /**
   * Propagates payment result to Order entity.
   *
   * <p>PAID → paymentStatus=PAID, orderStatus=CONFIRMED FAILED → paymentStatus=FAILED, orderStatus
   * stays PENDING (user can retry)
   */
  private void updateOrderStatus(Payment payment) {
    Order order = payment.getOrder();

    if (payment.getStatus() == PaymentStatus.PAID) {
      order.setPaymentStatus(PaymentStatus.PAID);
      order.setOrderStatus(OrderStatus.COMPLETED);
      log.info("Order #{} confirmed after payment", order.getOrderNumber());

    } else if (payment.getStatus() == PaymentStatus.FAILED) {
      order.setPaymentStatus(PaymentStatus.FAILED);
      // Don't auto-cancel order — let user retry with a new payment
      log.info("Order #{} payment failed", order.getOrderNumber());
    }

    orderRepository.save(order);
  }

  private InitiatePaymentResponse buildInitiateResponse(Payment payment) {
    var builder =
        InitiatePaymentResponse.builder()
            .paymentId(payment.getId())
            .gateway(payment.getGateway())
            .status(payment.getStatus())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .expiredAt(payment.getExpiredAt());

    switch (payment.getGateway()) {
      case BAKONG ->
          builder
              .bakongDeeplink(payment.getPaymentUrl())
              .message("Scan the QR or open the deeplink to pay via Bakong");
      case STRIPE ->
          builder
              .stripeClientSecret(payment.getPaymentUrl())
              .message("Use the clientSecret with Stripe.js to complete payment");
    }

    return builder.build();
  }

  private PaymentStatusResponse buildStatusResponse(Payment payment) {
    return PaymentStatusResponse.builder()
        .paymentId(payment.getId())
        .orderId(payment.getOrder().getId())
        .gateway(payment.getGateway())
        .status(payment.getStatus())
        .amount(payment.getAmount())
        .currency(payment.getCurrency())
        .paidAt(payment.getPaidAt())
        .message(
            payment.getStatus() == PaymentStatus.PAID
                ? "Payment successful"
                : payment.getStatus() == PaymentStatus.FAILED
                    ? "Payment failed"
                    : "Payment pending")
        .build();
  }
}
