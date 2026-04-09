package ecommerce_app.service.impl;

import ecommerce_app.entity.Order;
import ecommerce_app.repository.OrderRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNumberGenerator {

  private static final String ORDER_PREFIX = "ORD";
  private static final String POS_PREFIX = "POS";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final OrderRepository orderRepository;

  /** Generates a unique order number for regular online orders Format: ORD-YYYYMMDD-XXXX */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public String generateOrderNumber() {
    return generateOrderNumber(ORDER_PREFIX);
  }

  /** Generates a unique order number for POS (in-store) orders Format: POS-YYYYMMDD-XXXX */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public String generatePOSOrderNumber() {
    return generateOrderNumber(POS_PREFIX);
  }

  /** Generates a unique order number with specified prefix Format: {PREFIX}-YYYYMMDD-XXXX */
  private String generateOrderNumber(String prefix) {
    LocalDate today = LocalDate.now();
    String datePrefix = today.format(DATE_FORMATTER);

    // Get count of orders created today with this prefix
    Long todayCount = orderRepository.countOrdersCreatedTodayWithPrefix(today, prefix);

    // Generate sequential number for today (1-based)
    long sequenceNumber = (todayCount != null ? todayCount : 0L) + 1;

    // Format: PREFIX-YYYYMMDD-XXXX (supports up to 9999 orders per day)
    String orderNumber = String.format("%s-%s-%04d", prefix, datePrefix, sequenceNumber);

    log.debug(
        "Generated order number: {} (sequence: {} for date: {})",
        orderNumber,
        sequenceNumber,
        datePrefix);

    return orderNumber;
  }

  /** Alternative: Simple sequential format using database ID */
  public String generateSimpleOrderNumber(Order order) {
    if (order.getId() == null) {
      throw new IllegalArgumentException("Order must be saved before generating order number");
    }
    return String.format("%s-%06d", ORDER_PREFIX, order.getId());
  }
}
