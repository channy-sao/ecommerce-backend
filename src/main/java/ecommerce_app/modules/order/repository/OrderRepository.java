package ecommerce_app.modules.order.repository;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.modules.order.model.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ecommerce_app.modules.order.model.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository
    extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
  List<Order> findByUserId(Long userId);

  Optional<Order> findByIdAndUserId(Long orderId, Long userId);

  // for order statistics
  @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
  BigDecimal getTotalRevenue();

  @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate >= :startDate")
  BigDecimal getTotalRevenueSince(@Param("startDate") LocalDateTime startDate);

  @Query("SELECT COUNT(o) FROM Order o")
  Long getTotalOrderCount();

  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startDate")
  Long getOrderCountSince(@Param("startDate") LocalDateTime startDate);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
  Long getOrderCountByStatus(@Param("status") OrderStatus status);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status AND o.orderDate >= :startDate")
  Long getOrderCountByStatusSince(
      @Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate);

  @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o")
  BigDecimal getAverageOrderValue();

  @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o WHERE o.orderDate >= :startDate")
  BigDecimal getAverageOrderValueSince(@Param("startDate") LocalDateTime startDate);

  // Get stats for a specific period (for percentage calculations)
  @Query(
      "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
  BigDecimal getTotalRevenueBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
  Long getOrderCountBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
  BigDecimal getAverageOrderValueBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status AND o.orderDate BETWEEN :startDate AND :endDate")
  Long getOrderCountByStatusBetween(
      @Param("status") OrderStatus status,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);


    // Get orders by date range
    @Query("SELECT o FROM Order o WHERE DATE(o.orderDate) BETWEEN :fromDate AND :toDate")
    List<Order> findOrdersByDateRange(@Param("fromDate") LocalDate fromDate,
                                      @Param("toDate") LocalDate toDate);

    // Get orders by date range with pagination
    @Query("SELECT o FROM Order o WHERE DATE(o.orderDate) BETWEEN :fromDate AND :toDate " +
            "ORDER BY o.orderDate DESC")
    List<Order> findOrdersByDateRange(@Param("fromDate") LocalDate fromDate,
                                      @Param("toDate") LocalDate toDate,
                                      Pageable pageable);

    // Get order items by date range
    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE DATE(o.orderDate) BETWEEN :fromDate AND :toDate")
    List<OrderItem> findOrderItemsByDateRange(@Param("fromDate") LocalDate fromDate,
                                              @Param("toDate") LocalDate toDate);

}
