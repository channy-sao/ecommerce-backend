package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.NotificationType;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.dto.request.InitiatePaymentRequest;
import ecommerce_app.dto.request.NotificationRequest;
import ecommerce_app.dto.response.InitiatePaymentResponse;
import ecommerce_app.dto.response.PaymentStatusResponse;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.Payment;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.repository.PaymentRepository;
import ecommerce_app.service.PaymentService;
import ecommerce_app.service.strategy.PaymentGatewayStrategy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final NotificationService notificationService;
  private final Map<PaymentGateway, PaymentGatewayStrategy> strategies;

  public PaymentServiceImpl(
      PaymentRepository paymentRepository,
      OrderRepository orderRepository,
      NotificationService notificationService,
      List<PaymentGatewayStrategy> gatewayStrategies) {
    this.paymentRepository = paymentRepository;
    this.orderRepository = orderRepository;
    this.notificationService = notificationService;
    this.strategies =
        gatewayStrategies.stream()
            .collect(Collectors.toMap(PaymentGatewayStrategy::getGateway, Function.identity()));
  }

  // ─── 1. Initiate payment ─────────────────────────────────────────────────

  @Override
  public InitiatePaymentResponse initiate(InitiatePaymentRequest request, Long userId) {
    log.info("Initiating payment for order #{}", request.getOrderId());

    Order order =
        orderRepository
            .findByIdAndUserId(request.getOrderId(), userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

    // Guard: already paid
    if (order.getPaymentStatus() == PaymentStatus.PAID) {
      throw new BadRequestException("Order #" + order.getOrderNumber() + " is already paid.");
    }

    // Guard: cancelled order cannot be paid
    if (order.getOrderStatus() == OrderStatus.CANCELLED) {
      throw new BadRequestException(
          "Order #" + order.getOrderNumber() + " is cancelled and cannot be paid.");
    }

    // Auto-resolve gateway from order's paymentMethod if not explicitly provided in request
    PaymentGateway gateway =
        request.getGateway() != null
            ? request.getGateway()
            : PaymentGateway.fromPaymentMethod(order.getPaymentMethod());

    log.info("Resolved gateway: {} for order #{}", gateway, order.getOrderNumber());

    // Resolve strategy — throws UnsupportedOperationException for BAKONG
    PaymentGatewayStrategy strategy = resolveStrategy(gateway);

    // Delegate to strategy — builds Payment entity (not yet persisted)
    Payment payment = strategy.initiate(order, request);
    payment.setOrder(order);

    // Persist
    Payment savedPayment = paymentRepository.save(payment);
    log.info("Payment #{} created for order #{}", savedPayment.getId(), order.getOrderNumber());

    // For COD and CASH_IN_SHOP: confirm order immediately (no waiting for gateway callback)
    if (gateway == PaymentGateway.COD || gateway == PaymentGateway.CASH_IN_SHOP) {
      order.setOrderStatus(OrderStatus.CONFIRMED);
      orderRepository.save(order);
      log.info("Order #{} confirmed ({})", order.getOrderNumber(), gateway);

      sendNotification(
          order.getUser().getId(),
          "Order Confirmed!",
          "Your order #"
              + order.getOrderNumber()
              + " is confirmed. "
              + (gateway == PaymentGateway.COD
                  ? "Pay when your package arrives."
                  : "Please visit our store within 24 hours to pay and collect."),
          NotificationType.ORDER_CONFIRMED,
          order.getId());
    }

    return buildInitiateResponse(savedPayment, gateway);
  }

  // ─── 2. Poll payment status ───────────────────────────────────────────────

  @Override
  @Transactional
  public PaymentStatusResponse getStatus(Long paymentId, Long userId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

    if (!payment.getOrder().getUser().getId().equals(userId)) {
      throw new SecurityException("Access denied to payment " + paymentId);
    }

    if (payment.getStatus() == PaymentStatus.PENDING) {
      PaymentGatewayStrategy strategy = resolveStrategy(payment.getGateway());
      strategy.syncStatus(payment);
      paymentRepository.save(payment);

      if (payment.getStatus() == PaymentStatus.PAID
          || payment.getStatus() == PaymentStatus.FAILED) {
        updateOrderFromPayment(payment);
      }
    }

    return buildStatusResponse(payment);
  }

  // ─── 3. Confirm payment (webhook / internal) ──────────────────────────────

  @Override
  @Transactional
  public void confirmPayment(Payment payment) {
    if (payment.getStatus() == PaymentStatus.PAID) {
      log.info("Payment #{} already confirmed — skipping", payment.getId());
      return;
    }
    paymentRepository.save(payment);
    updateOrderFromPayment(payment);
    log.info("Payment #{} confirmed with status {}", payment.getId(), payment.getStatus());
  }

  // ─── 4. Staff: mark COD as paid ──────────────────────────────────────────

  @Override
  @Transactional
  public void markCodPaid(Long orderId, Long staffUserId) {
    log.info("Staff {} marking COD payment as paid for order #{}", staffUserId, orderId);

    Payment payment = getLatestPendingPayment(orderId, PaymentGateway.COD);

    payment.setStatus(PaymentStatus.PAID);
    payment.setPaidAt(LocalDateTime.now());
    paymentRepository.save(payment);

    Order order = payment.getOrder();
    order.setPaymentStatus(PaymentStatus.PAID);
    order.setOrderStatus(OrderStatus.DELIVERED);
    orderRepository.save(order);

    sendNotification(
        order.getUser().getId(),
        "Payment Received",
        "Cash payment received for order #" + order.getOrderNumber() + ". Thank you!",
        NotificationType.PAYMENT_SUCCESS,
        order.getId());

    log.info("COD payment marked as paid for order #{}", order.getOrderNumber());
  }

  // ─── 5. Staff: mark Cash-in-Shop as paid ─────────────────────────────────

  @Override
  @Transactional
  public void markCashInShopPaid(Long orderId, Long staffUserId) {
    log.info("Staff {} marking Cash-in-Shop payment as paid for order #{}", staffUserId, orderId);

    Payment payment = getLatestPendingPayment(orderId, PaymentGateway.CASH_IN_SHOP);

    if (payment.getExpiredAt() != null && LocalDateTime.now().isAfter(payment.getExpiredAt())) {
      throw new BadRequestException(
          "Order #" + payment.getOrder().getOrderNumber() + " reservation has expired.");
    }

    payment.setStatus(PaymentStatus.PAID);
    payment.setPaidAt(LocalDateTime.now());
    paymentRepository.save(payment);

    Order order = payment.getOrder();
    order.setPaymentStatus(PaymentStatus.PAID);
    order.setOrderStatus(OrderStatus.COMPLETED);
    orderRepository.save(order);

    sendNotification(
        order.getUser().getId(),
        "Payment Received",
        "Payment received for order #" + order.getOrderNumber() + ". Enjoy your purchase!",
        NotificationType.PAYMENT_SUCCESS,
        order.getId());

    log.info("Cash-in-Shop payment marked as paid for order #{}", order.getOrderNumber());
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  private PaymentGatewayStrategy resolveStrategy(PaymentGateway gateway) {
    PaymentGatewayStrategy strategy = strategies.get(gateway);
    if (strategy == null) {
      throw new IllegalArgumentException("Unsupported payment gateway: " + gateway);
    }
    return strategy;
  }

  private void updateOrderFromPayment(Payment payment) {
    Order order = payment.getOrder();

    if (payment.getStatus() == PaymentStatus.PAID) {
      order.setPaymentStatus(PaymentStatus.PAID);
      order.setOrderStatus(OrderStatus.CONFIRMED);
      log.info("Order #{} confirmed after payment", order.getOrderNumber());

      sendNotification(
          order.getUser().getId(),
          "Payment Successful!",
          "Payment for order #"
              + order.getOrderNumber()
              + " was successful. We are preparing your order.",
          NotificationType.PAYMENT_SUCCESS,
          order.getId());

    } else if (payment.getStatus() == PaymentStatus.FAILED) {
      order.setPaymentStatus(PaymentStatus.FAILED);
      log.info("Order #{} payment failed — user can retry", order.getOrderNumber());

      sendNotification(
          order.getUser().getId(),
          "Payment Failed",
          "Payment for order #" + order.getOrderNumber() + " failed. Please try again.",
          NotificationType.PAYMENT_FAILED,
          order.getId());
    }

    orderRepository.save(order);
  }

  private Payment getLatestPendingPayment(Long orderId, PaymentGateway gateway) {
    return paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING).stream()
        .filter(p -> p.getGateway() == gateway)
        .findFirst()
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "No pending " + gateway + " payment found for order: " + orderId));
  }

  private void sendNotification(
      Long userId, String title, String message, NotificationType type, Long orderId) {
    try {
      NotificationRequest notifRequest =
          NotificationRequest.builder()
              .userId(userId)
              .title(title)
              .message(message)
              .type(type)
              .referenceId(String.valueOf(orderId))
              .referenceType("ORDER")
              .sendPush(true)
              .saveToDatabase(true)
              .expiresInDays(30)
              .build();
      notificationService.createAndSendNotification(notifRequest);
    } catch (Exception e) {
      log.warn("Failed to send payment notification: {}", e.getMessage());
    }
  }

  private InitiatePaymentResponse buildInitiateResponse(Payment payment, PaymentGateway gateway) {
    var builder =
        InitiatePaymentResponse.builder()
            .paymentId(payment.getId())
            .gateway(gateway)
            .status(payment.getStatus())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .expiredAt(payment.getExpiredAt());

    switch (gateway) {
      case BAKONG ->
          builder
              .bakongDeeplink(payment.getPaymentUrl())
              .message("Scan the QR or open the deeplink to pay via Bakong / KHQR");
      case COD ->
          builder.message("Order confirmed. Please prepare cash when your package arrives.");
      case CASH_IN_SHOP ->
          builder.message(
              "Order confirmed. Please visit our store within 24 hours to pay and collect.");
    }

    return builder.build();
  }

  private PaymentStatusResponse buildStatusResponse(Payment payment) {
    String message =
        switch (payment.getStatus()) {
          case PAID -> "Payment successful";
          case FAILED -> "Payment failed";
          case CANCELLED -> "Payment cancelled";
          default -> "Payment pending";
        };

    return PaymentStatusResponse.builder()
        .paymentId(payment.getId())
        .orderId(payment.getOrder().getId())
        .gateway(payment.getGateway())
        .status(payment.getStatus())
        .amount(payment.getAmount())
        .currency(payment.getCurrency())
        .paidAt(payment.getPaidAt())
        .message(message)
        .build();
  }
}
