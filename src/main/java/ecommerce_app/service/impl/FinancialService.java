package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.TransactionStatus;
import ecommerce_app.constant.enums.TransactionType;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.Payment;
import ecommerce_app.entity.PaymentTransaction;
import ecommerce_app.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialService {

  private final PaymentTransactionRepository transactionRepository;

  @Transactional
  public PaymentTransaction recordCashPayment(
      Order order, Payment payment, Long cashierUserId, String cashierName) {
    return recordCashPayment(
        order, payment, cashierUserId, cashierName, order.getTotalAmount(), null);
  }

  @Transactional
  public PaymentTransaction recordCashPayment(
      Order order,
      Payment payment,
      Long cashierUserId,
      String cashierName,
      BigDecimal amount,
      String note) {

    log.info("Recording cash payment for order: {}, Amount: {}", order.getOrderNumber(), amount);

    // Generate unique receipt number
    String receiptNumber = generateReceiptNumber(order.getPaymentMethod(), order.getId());

    // Create transaction record
    PaymentTransaction transaction =
        PaymentTransaction.builder()
            .payment(payment)
            .order(order)
            .type(TransactionType.CAPTURE)
            .paymentMethod(order.getPaymentMethod())
            .amount(amount != null ? amount : order.getTotalAmount())
            .currency("USD")
            .status(TransactionStatus.COMPLETED)
            .referenceNumber(receiptNumber)
            .cashierName(cashierName != null ? cashierName : "System")
            .cashierUserId(cashierUserId)
            .transactionDate(LocalDateTime.now())
            .notes(
                note != null
                    ? note
                    : String.format(
                        "Cash payment collected via %s for order #%s",
                        order.getPaymentMethod(), order.getOrderNumber()))
            .build();

    return transactionRepository.save(transaction);
  }

  @Transactional
  public PaymentTransaction recordPaymentRefund(
      Order order, Payment payment, Long staffUserId, String staffName, String reason) {
    log.info("Recording refund for order: {}", order.getOrderNumber());

    String refundNumber = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    PaymentTransaction transaction =
        PaymentTransaction.builder()
            .payment(payment)
            .order(order)
            .type(TransactionType.REFUND)
            .paymentMethod(order.getPaymentMethod())
            .amount(order.getTotalAmount())
            .currency("USD")
            .status(TransactionStatus.COMPLETED)
            .referenceNumber(refundNumber)
            .cashierName(staffName)
            .cashierUserId(staffUserId)
            .transactionDate(LocalDateTime.now())
            .notes("Refund issued: " + reason)
            .build();

    return transactionRepository.save(transaction);
  }

  private String generateReceiptNumber(PaymentMethod method, Long orderId) {
    String prefix = method == PaymentMethod.COD ? "COD" : "CASH";
    String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
    String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    return String.format("%s-%d-%s-%s", prefix, orderId, timestamp, random);
  }
}
