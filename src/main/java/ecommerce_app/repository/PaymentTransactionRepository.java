package ecommerce_app.repository;

import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

  Optional<PaymentTransaction> findByReferenceNumber(String referenceNumber);

  List<PaymentTransaction> findByOrderId(Long orderId);

  List<PaymentTransaction> findByPaymentId(Long paymentId);

  // ADD THESE NEW METHODS
  @Query(
      "SELECT SUM(t.amount) FROM PaymentTransaction t "
          + "WHERE DATE(t.transactionDate) = :date "
          + "AND t.paymentMethod = :paymentMethod "
          + "AND t.status = 'COMPLETED'")
  BigDecimal getTotalCashCollectedByDate(
      @Param("date") LocalDate date, @Param("paymentMethod") PaymentMethod paymentMethod);

  @Query(
      "SELECT COUNT(DISTINCT t.order.id) FROM PaymentTransaction t "
          + "WHERE DATE(t.transactionDate) = :date "
          + "AND t.paymentMethod = :paymentMethod "
          + "AND t.status = 'COMPLETED'")
  Integer getOrderCountByDateAndMethod(
      @Param("date") LocalDate date, @Param("paymentMethod") PaymentMethod paymentMethod);

  @Query(
      "SELECT t.cashierUserId, t.cashierName, SUM(t.amount), COUNT(t) FROM PaymentTransaction t "
          + "WHERE DATE(t.transactionDate) = :date "
          + "AND t.status = 'COMPLETED' "
          + "GROUP BY t.cashierUserId, t.cashierName")
  List<Object[]> getCashierSummaryByDate(@Param("date") LocalDate date);

  List<PaymentTransaction> findByCashierUserIdAndTransactionDateBetween(
      Long cashierUserId, LocalDateTime startDate, LocalDateTime endDate);

  @Query(
      "SELECT t FROM PaymentTransaction t "
          + "WHERE t.transactionDate BETWEEN :startDate AND :endDate "
          + "AND t.status = 'COMPLETED'")
  List<PaymentTransaction> findTransactionsBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
