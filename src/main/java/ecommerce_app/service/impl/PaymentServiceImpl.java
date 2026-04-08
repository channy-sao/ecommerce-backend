package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.NotificationType;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.dto.request.InitiatePaymentRequest;
import ecommerce_app.dto.request.NotificationRequest;
import ecommerce_app.dto.response.DailyCashSummary;
import ecommerce_app.dto.response.InitiatePaymentResponse;
import ecommerce_app.dto.response.PaymentStatusResponse;
import ecommerce_app.dto.response.PaymentTransactionResponse;
import ecommerce_app.dto.response.ReceiptItem;
import ecommerce_app.dto.response.ReceiptResponse;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.Payment;
import ecommerce_app.entity.PaymentTransaction;
import ecommerce_app.entity.User;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.repository.PaymentRepository;
import ecommerce_app.repository.PaymentTransactionRepository;
import ecommerce_app.repository.UserRepository;
import ecommerce_app.service.PaymentService;
import ecommerce_app.service.strategy.PaymentGatewayStrategy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private final FinancialService financialService;
  private final UserRepository userRepository;
  private final PaymentTransactionRepository transactionRepository;
  private final Map<PaymentGateway, PaymentGatewayStrategy> strategies;

  public PaymentServiceImpl(
      PaymentRepository paymentRepository,
      OrderRepository orderRepository,
      NotificationService notificationService,
      FinancialService financialService,
      UserRepository userRepository,
      PaymentTransactionRepository transactionRepository,
      List<PaymentGatewayStrategy> gatewayStrategies) {
    this.paymentRepository = paymentRepository;
    this.orderRepository = orderRepository;
    this.notificationService = notificationService;
    this.financialService = financialService;
    this.userRepository = userRepository;
    this.transactionRepository = transactionRepository;
    this.strategies =
        gatewayStrategies.stream()
            .collect(Collectors.toMap(PaymentGatewayStrategy::getGateway, Function.identity()));
  }

  // PaymentServiceImpl.java
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

    // Check if payment already exists for this order
    Optional<Payment> existingPayment = paymentRepository.findByOrderId(order.getId());
    if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.PENDING) {
      log.info(
          "Payment already exists for order #{}, returning existing payment",
          order.getOrderNumber());
      return buildInitiateResponse(existingPayment.get(), existingPayment.get().getGateway());
    }

    // Auto-resolve gateway from order's paymentMethod
    PaymentGateway gateway =
        request.getGateway() != null
            ? request.getGateway()
            : PaymentGateway.fromPaymentMethod(order.getPaymentMethod());

    log.info("Resolved gateway: {} for order #{}", gateway, order.getOrderNumber());

    // Resolve strategy
    PaymentGatewayStrategy strategy = resolveStrategy(gateway);

    // Delegate to strategy — builds Payment entity
    Payment payment = strategy.initiate(order, request);
    payment.setOrder(order);

    // Persist - THIS IS CRITICAL
    Payment savedPayment = paymentRepository.save(payment);
    log.info("Payment #{} created for order #{}", savedPayment.getId(), order.getOrderNumber());

    // Update order with payment reference if needed
    // order.setPayment(savedPayment); // If you have this relationship

    return buildInitiateResponse(savedPayment, gateway);
  }

  @Override
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

  @Override
  public void confirmPayment(Payment payment) {
    if (payment.getStatus() == PaymentStatus.PAID) {
      log.info("Payment #{} already confirmed — skipping", payment.getId());
      return;
    }
    paymentRepository.save(payment);
    updateOrderFromPayment(payment);
    log.info("Payment #{} confirmed with status {}", payment.getId(), payment.getStatus());
  }

  @Override
  @Transactional
  public void markCodPaid(Long orderId, Long staffUserId) {
    log.info("Staff {} marking COD payment as paid for order #{}", staffUserId, orderId);

    // Get the pending COD payment
    Payment payment = getLatestPendingPayment(orderId, PaymentGateway.COD);
    Order order = payment.getOrder();

    // Verify order is in correct state
    if (order.getOrderStatus() == OrderStatus.DELIVERED) {
      log.warn("Order #{} already marked as delivered", order.getOrderNumber());
      throw new BadRequestException("Order already delivered");
    }

    // Get staff info for recording
    String staffName = getStaffName(staffUserId);

    // Update payment status
    payment.setStatus(PaymentStatus.PAID);
    payment.setPaidAt(LocalDateTime.now());
    paymentRepository.save(payment);

    // Update order status
    order.setPaymentStatus(PaymentStatus.PAID);
    order.setOrderStatus(OrderStatus.DELIVERED);
    orderRepository.save(order);

    // 🔴 RECORD THE CASH TRANSACTION
    PaymentTransaction transaction =
        financialService.recordCashPayment(order, payment, staffUserId, staffName);

    log.info(
        "✅ COD payment recorded - Receipt: {} for order #{}",
        transaction.getReferenceNumber(),
        order.getOrderNumber());

    // Send notification with receipt
    sendNotification(
        order.getUser().getId(),
        "Payment Received - COD",
        String.format(
            "Cash payment of $%.2f received for order #%s.\nReceipt: %s\nThank you for your purchase!",
            order.getTotalAmount(), order.getOrderNumber(), transaction.getReferenceNumber()),
        NotificationType.PAYMENT_SUCCESS,
        order.getId());
  }

  @Override
  @Transactional
  public void markCashInShopPaid(Long orderId, Long staffUserId) {
    log.info("Staff {} marking Cash-in-Shop payment as paid for order #{}", staffUserId, orderId);

    // Get the pending Cash-in-Shop payment
    Payment payment = getLatestPendingPayment(orderId, PaymentGateway.CASH_IN_SHOP);
    Order order = payment.getOrder();

    // Check if payment has expired
    if (payment.getExpiredAt() != null && LocalDateTime.now().isAfter(payment.getExpiredAt())) {
      throw new BadRequestException(
          "Order #"
              + order.getOrderNumber()
              + " reservation has expired. Please create a new order.");
    }

    // Verify order is in correct state
    if (order.getOrderStatus() != OrderStatus.CONFIRMED
        && order.getOrderStatus() != OrderStatus.PROCESSING
        && order.getOrderStatus() != OrderStatus.READY_FOR_PICKUP) {
      throw new BadRequestException(
          "Order #"
              + order.getOrderNumber()
              + " is not ready for pickup. Current status: "
              + order.getOrderStatus());
    }

    // Get staff info for recording
    String staffName = getStaffName(staffUserId);

    // Update payment status
    payment.setStatus(PaymentStatus.PAID);
    payment.setPaidAt(LocalDateTime.now());
    paymentRepository.save(payment);

    // Update order status
    order.setPaymentStatus(PaymentStatus.PAID);
    order.setOrderStatus(OrderStatus.COMPLETED);
    orderRepository.save(order);

    // 🔴 RECORD THE CASH TRANSACTION
    PaymentTransaction transaction =
        financialService.recordCashPayment(order, payment, staffUserId, staffName);

    log.info(
        "✅ Cash-in-Shop payment recorded - Receipt: {} for order #{}",
        transaction.getReferenceNumber(),
        order.getOrderNumber());

    // Send notification with receipt
    sendNotification(
        order.getUser().getId(),
        "Payment Received - Store Pickup",
        String.format(
            "Payment of $%.2f received at store for order #%s.\nReceipt: %s\nPlease keep this receipt for your records.\nThank you for shopping with us!",
            order.getTotalAmount(), order.getOrderNumber(), transaction.getReferenceNumber()),
        NotificationType.PAYMENT_SUCCESS,
        order.getId());
  }

  // ─── Private Helper Methods ─────────────────────────────────────────────

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
    List<Payment> payments =
        paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING);

    // Try to find existing pending payment
    Optional<Payment> existingPayment =
        payments.stream().filter(p -> p.getGateway() == gateway).findFirst();

    if (existingPayment.isPresent()) {
      return existingPayment.get();
    }

    // If no payment found, create one automatically
    log.warn(
        "No pending {} payment found for order: {}. Creating one automatically.", gateway, orderId);

    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

    // Create payment based on gateway
    Payment newPayment = createPaymentForOrder(order, gateway);
    Payment savedPayment = paymentRepository.save(newPayment);

    log.info(
        "Auto-created payment #{} for order #{}", savedPayment.getId(), order.getOrderNumber());

    return savedPayment;
  }

  private Payment createPaymentForOrder(Order order, PaymentGateway gateway) {
    Payment payment = new Payment();
    payment.setOrder(order);
    payment.setGateway(gateway);
    payment.setAmount(order.getTotalAmount());
    payment.setCurrency("USD");
    payment.setStatus(PaymentStatus.PENDING);
    payment.setCreatedAt(LocalDateTime.now());

    if (gateway == PaymentGateway.CASH_IN_SHOP) {
      // Set expiry for Cash-in-Shop (24 hours)
      payment.setExpiredAt(LocalDateTime.now().plusHours(24));
      payment.setGatewayReference("SHOP-" + order.getOrderNumber());
    } else if (gateway == PaymentGateway.COD) {
      payment.setGatewayReference("COD-" + order.getOrderNumber());
    }

    return payment;
  }

  private String getStaffName(Long staffUserId) {
    return userRepository
        .findById(staffUserId)
        .map(
            user -> {
              if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                return user.getFullName();
              }
              return user.getEmail();
            })
        .orElse("Staff-" + staffUserId);
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
      case BAKONG:
        builder
            .bakongDeeplink(payment.getPaymentUrl())
            .message("Scan the QR or open the deeplink to pay via Bakong / KHQR");
        break;
      case COD:
        builder.message(
            "Order confirmed. Please prepare $"
                + payment.getAmount()
                + " cash when your package arrives.");
        break;
      case CASH_IN_SHOP:
        builder.message(
            "Order confirmed. Please visit our store within 24 hours to pay $"
                + payment.getAmount()
                + " and collect your items.");
        break;
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

  // Add these methods to your PaymentServiceImpl.java

  @Override
  public DailyCashSummary getDailyCashSummary(LocalDate date) {
    log.info("Getting daily cash summary for date: {}", date);

    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);

    // Get COD totals
    BigDecimal totalCodCollected =
        transactionRepository.getTotalCashCollectedByDate(date, PaymentMethod.COD);
    Integer totalCodOrders =
        transactionRepository.getOrderCountByDateAndMethod(date, PaymentMethod.COD);

    // Get Cash-in-Shop totals
    BigDecimal totalCashInShopCollected =
        transactionRepository.getTotalCashCollectedByDate(date, PaymentMethod.CASH_IN_SHOP);
    Integer totalCashInShopOrders =
        transactionRepository.getOrderCountByDateAndMethod(date, PaymentMethod.CASH_IN_SHOP);

    // Get cashier summaries
    List<DailyCashSummary.CashierSummary> cashierSummaries = getCashierSummaries(date);

    BigDecimal grandTotal = BigDecimal.ZERO;
    if (totalCodCollected != null) grandTotal = grandTotal.add(totalCodCollected);
    if (totalCashInShopCollected != null) grandTotal = grandTotal.add(totalCashInShopCollected);

    return DailyCashSummary.builder()
        .date(date)
        .totalCodCollected(totalCodCollected != null ? totalCodCollected : BigDecimal.ZERO)
        .totalCashInShopCollected(
            totalCashInShopCollected != null ? totalCashInShopCollected : BigDecimal.ZERO)
        .grandTotal(grandTotal)
        .totalCodOrders(totalCodOrders != null ? totalCodOrders : 0)
        .totalCashInShopOrders(totalCashInShopOrders != null ? totalCashInShopOrders : 0)
        .cashiers(cashierSummaries)
        .build();
  }

  @Override
  public DailyCashSummary getDailyCashSummaryByCashier(LocalDate date, Long cashierId) {
    log.info("Getting daily cash summary for cashier: {} on date: {}", cashierId, date);

    List<PaymentTransaction> transactions =
        transactionRepository.findByCashierUserIdAndTransactionDateBetween(
            cashierId, date.atStartOfDay(), date.atTime(23, 59, 59));

    BigDecimal totalCod =
        transactions.stream()
            .filter(t -> t.getPaymentMethod() == PaymentMethod.COD)
            .map(PaymentTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalCashInShop =
        transactions.stream()
            .filter(t -> t.getPaymentMethod() == PaymentMethod.CASH_IN_SHOP)
            .map(PaymentTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    long codCount =
        transactions.stream().filter(t -> t.getPaymentMethod() == PaymentMethod.COD).count();
    long cashInShopCount =
        transactions.stream()
            .filter(t -> t.getPaymentMethod() == PaymentMethod.CASH_IN_SHOP)
            .count();

    return DailyCashSummary.builder()
        .date(date)
        .totalCodCollected(totalCod)
        .totalCashInShopCollected(totalCashInShop)
        .grandTotal(totalCod.add(totalCashInShop))
        .totalCodOrders((int) codCount)
        .totalCashInShopOrders((int) cashInShopCount)
        .build();
  }

  @Override
  public List<PaymentTransactionResponse> getTransactionsByOrder(Long orderId) {
    List<PaymentTransaction> transactions = transactionRepository.findByOrderId(orderId);
    if (transactions.isEmpty()) {
      throw new ResourceNotFoundException("No transactions found for order: " + orderId);
    }
    return transactions.stream().map(this::toTransactionResponse).collect(Collectors.toList());
  }

  @Override
  public PaymentTransactionResponse getTransactionByReceipt(String receiptNumber) {
    PaymentTransaction transaction =
        transactionRepository
            .findByReferenceNumber(receiptNumber)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No transaction found for receipt: " + receiptNumber));
    return toTransactionResponse(transaction);
  }

  @Override
  public ReceiptResponse getReceiptByOrder(Long orderId) {
    List<PaymentTransaction> transactions = transactionRepository.findByOrderId(orderId);
    if (transactions.isEmpty()) {
      throw new ResourceNotFoundException("No transaction found for order: " + orderId);
    }
    return buildReceiptResponse(transactions.getFirst());
  }

  @Override
  public List<ReceiptResponse> getReceiptsByDateRange(LocalDateTime start, LocalDateTime end) {
    return transactionRepository.findTransactionsBetween(start, end).stream()
        .map(this::buildReceiptResponse)
        .collect(Collectors.toList());
  }

  // Move mapping methods here from controller
  private PaymentTransactionResponse toTransactionResponse(PaymentTransaction transaction) {
    return PaymentTransactionResponse.builder()
        .id(transaction.getId())
        .referenceNumber(transaction.getReferenceNumber())
        .amount(transaction.getAmount())
        .paymentMethod(transaction.getPaymentMethod().name())
        .transactionType(transaction.getType().name())
        .status(transaction.getStatus().name())
        .cashierName(transaction.getCashierName())
        .transactionDate(transaction.getTransactionDate())
        .notes(transaction.getNotes())
        .orderId(transaction.getOrder().getId())
        .orderNumber(transaction.getOrder().getOrderNumber())
        .build();
  }

  private ReceiptResponse buildReceiptResponse(PaymentTransaction transaction) {
    Order order = transaction.getOrder();
    User user = order.getUser();

    return ReceiptResponse.builder()
        .receiptNumber(transaction.getReferenceNumber())
        .orderNumber(order.getOrderNumber())
        .date(transaction.getTransactionDate())
        .amount(transaction.getAmount())
        .paymentMethod(transaction.getPaymentMethod())
        .cashierName(transaction.getCashierName())
        .customerName(user.getFullName())
        .customerEmail(user.getEmail())
        .status(order.getPaymentStatus())
        .items(
            order.getOrderItems().stream()
                .map(
                    item ->
                        ReceiptItem.builder()
                            .productName(item.getProduct().getName())
                            .productCode(item.getProduct().getCode())
                            .quantity(item.getQuantity())
                            .price(item.getOriginalPrice())
                            .total(item.getTotalPrice())
                            .build())
                .collect(Collectors.toList()))
        .subtotal(order.getSubtotalAmount())
        .discount(order.getDiscountAmount())
        .shipping(order.getShippingCost())
        .total(order.getTotalAmount())
        .build();
  }

  private List<DailyCashSummary.CashierSummary> getCashierSummaries(LocalDate date) {
    List<Object[]> results = transactionRepository.getCashierSummaryByDate(date);

    return results.stream()
        .map(
            row ->
                DailyCashSummary.CashierSummary.builder()
                    .cashierId(((Number) row[0]).longValue())
                    .cashierName((String) row[1])
                    .totalCollected((BigDecimal) row[2])
                    .orderCount(((Number) row[3]).intValue())
                    .build())
        .collect(Collectors.toList());
  }
}
