package ecommerce_app.modules.order.service.impl;

import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.order.repository.OrderRepository;

import java.time.Instant;
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
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final OrderRepository orderRepository;

  /**
   * Generates a unique order number in format: ORD-YYYYMMDD-XXXX
   * Example: ORD-20240208-0001
   * 
   * This format provides:
   * - Easy date identification
   * - Daily sequential numbering
   * - Human-readable format
   * - Uniqueness guaranteed by database constraint
   *
   * @return unique order number
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public String generateOrderNumber() {
    LocalDate today = LocalDate.now();
    String datePrefix = today.format(DATE_FORMATTER);
    
    // Get count of orders created today
    Long todayCount = orderRepository.countOrdersCreatedToday(Instant.from(today));
    
    // Generate sequential number for today (1-based)
    long sequenceNumber = (todayCount != null ? todayCount : 0L) + 1;
    
    // Format: ORD-YYYYMMDD-XXXX (4 digits, supports up to 9999 orders per day)
    String orderNumber = String.format("%s-%s-%04d", ORDER_PREFIX, datePrefix, sequenceNumber);
    
    log.debug("Generated order number: {} (sequence: {} for date: {})", 
              orderNumber, sequenceNumber, datePrefix);
    
    return orderNumber;
  }

  /**
   * Alternative: Simple sequential format using database ID
   * Format: ORD-XXXXXX
   * Use this if you prefer simpler format without date
   *
   * @param order the saved order with ID
   * @return order number based on ID
   */
  public String generateSimpleOrderNumber(Order order) {
    if (order.getId() == null) {
      throw new IllegalArgumentException("Order must be saved before generating order number");
    }
    return String.format("%s-%06d", ORDER_PREFIX, order.getId());
  }
}