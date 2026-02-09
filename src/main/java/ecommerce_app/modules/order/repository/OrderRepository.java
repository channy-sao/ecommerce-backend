package ecommerce_app.modules.order.repository;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.modules.order.model.entity.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.modules.order.model.projection.OrderStatsProjection;
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
  BigDecimal getTotalRevenueSince(@Param("startDate") Instant startDate);

  @Query("SELECT COUNT(o) FROM Order o")
  Long getTotalOrderCount();

  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startDate")
  Long getOrderCountSince(@Param("startDate") Instant startDate);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
  Long getOrderCountByStatus(@Param("status") OrderStatus status);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status AND o.orderDate >= :startDate")
  Long getOrderCountByStatusSince(
      @Param("status") OrderStatus status, @Param("startDate") Instant startDate);

  @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o")
  BigDecimal getAverageOrderValue();

  @Query("SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o WHERE o.orderDate >= :startDate")
  BigDecimal getAverageOrderValueSince(@Param("startDate") Instant startDate);

  // Get stats for a specific period (for percentage calculations)
  @Query(
      "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
  BigDecimal getTotalRevenueBetween(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
  Long getOrderCountBetween(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      "SELECT COALESCE(AVG(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
  BigDecimal getAverageOrderValueBetween(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query(
      "SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status AND o.orderDate BETWEEN :startDate AND :endDate")
  Long getOrderCountByStatusBetween(
      @Param("status") OrderStatus status,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  // Get orders by date range
  @Query("SELECT o FROM Order o WHERE DATE(o.orderDate) BETWEEN :fromDate AND :toDate")
  List<Order> findOrdersByDateRange(
      @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

  // Get orders by date range with pagination
  @Query(
      "SELECT o FROM Order o WHERE DATE(o.orderDate) BETWEEN :fromDate AND :toDate "
          + "ORDER BY o.orderDate DESC")
  List<Order> findOrdersByDateRange(
      @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

  // Get order items by date range
  @Query(
      "SELECT oi FROM OrderItem oi "
          + "JOIN oi.order o "
          + "WHERE DATE(o.orderDate) BETWEEN :fromDate AND :toDate")
  List<OrderItem> findOrderItemsByDateRange(
      @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

  @Query(
      """
    SELECT
        COALESCE(SUM(CASE WHEN o.orderDate >= :currentStart THEN o.totalAmount END), 0) as currentRevenue,
        COUNT(CASE WHEN o.orderDate >= :currentStart THEN 1 END) as currentOrders,
        COUNT(CASE WHEN o.orderDate >= :currentStart AND o.orderStatus = 'PENDING' THEN 1 END) as currentPending,
        COUNT(CASE WHEN o.orderDate >= :currentStart AND o.orderStatus = 'PROCESSING' THEN 1 END) as currentProcessing,
        COUNT(CASE WHEN o.orderDate >= :currentStart AND o.orderStatus = 'SHIPPED' THEN 1 END) as currentShipped,
        COUNT(CASE WHEN o.orderDate >= :currentStart AND o.orderStatus = 'DELIVERED' THEN 1 END) as currentDelivered,
        COUNT(CASE WHEN o.orderDate >= :currentStart AND o.orderStatus = 'COMPLETED' THEN 1 END) as currentCompleted,
        COUNT(CASE WHEN o.orderDate >= :currentStart AND o.orderStatus = 'CANCELLED' THEN 1 END) as currentCancelled,
        COALESCE(SUM(CASE WHEN o.orderDate >= :previousStart AND o.orderDate < :currentStart THEN o.totalAmount END), 0) as previousRevenue,
        COUNT(CASE WHEN o.orderDate >= :previousStart AND o.orderDate < :currentStart THEN 1 END) as previousOrders,
        COUNT(CASE WHEN o.orderDate >= :previousStart AND o.orderDate < :currentStart AND o.orderStatus = 'PENDING' THEN 1 END) as previousPending,
        COUNT(CASE WHEN o.orderDate >= :previousStart AND o.orderDate < :currentStart AND o.orderStatus = 'COMPLETED' THEN 1 END) as previousCompleted,
        COUNT(CASE WHEN o.orderDate >= :previousStart AND o.orderDate < :currentStart AND o.orderStatus = 'CANCELLED' THEN 1 END) as previousCancelled
    FROM Order o
    WHERE o.orderDate >= :previousStart
    """)
  OrderStatsProjection getAggregatedDashboardStats(
      @Param("previousStart") Instant previousStart, @Param("currentStart") Instant currentStart);

  @Query(
      """
    SELECT
        COALESCE(SUM(CASE WHEN o.orderDate BETWEEN :currentStart AND :currentEnd THEN o.totalAmount END), 0) as currentRevenue,
        COUNT(CASE WHEN o.orderDate BETWEEN :currentStart AND :currentEnd THEN 1 END) as currentOrders,
        COUNT(CASE WHEN o.orderDate BETWEEN :currentStart AND :currentEnd AND o.orderStatus = 'PENDING' THEN 1 END) as currentPending,
        COUNT(CASE WHEN o.orderDate BETWEEN :currentStart AND :currentEnd AND o.orderStatus = 'PROCESSING' THEN 1 END) as currentProcessing,
        COUNT(CASE WHEN o.orderDate BETWEEN :currentStart AND :currentEnd AND o.orderStatus = 'SHIPPED' THEN 1 END) as currentShipped,
        COUNT(CASE WHEN o.orderDate BETWEEN :currentStart AND :currentEnd AND o.orderStatus = 'DELIVERED' THEN 1 END) as currentDelivered,
        COUNT(CASE WHEN o.orderDate BETWEEN :currentStart AND :currentEnd AND o.orderStatus = 'COMPLETED' THEN 1 END) as currentCompleted,
        COUNT(CASE WHEN o.orderDate BETWEEN :currentStart AND :currentEnd AND o.orderStatus = 'CANCELLED' THEN 1 END) as currentCancelled,
        COALESCE(SUM(CASE WHEN o.orderDate BETWEEN :previousStart AND :previousEnd THEN o.totalAmount END), 0) as previousRevenue,
        COUNT(CASE WHEN o.orderDate BETWEEN :previousStart AND :previousEnd THEN 1 END) as previousOrders,
        COUNT(CASE WHEN o.orderDate BETWEEN :previousStart AND :previousEnd AND o.orderStatus = 'PENDING' THEN 1 END) as previousPending,
        COUNT(CASE WHEN o.orderDate BETWEEN :previousStart AND :previousEnd AND o.orderStatus = 'COMPLETED' THEN 1 END) as previousCompleted,
        COUNT(CASE WHEN o.orderDate BETWEEN :previousStart AND :previousEnd AND o.orderStatus = 'CANCELLED' THEN 1 END) as previousCancelled
    FROM Order o
    WHERE o.orderDate BETWEEN :previousStart AND :currentEnd
    """)
  OrderStatsProjection getAggregatedStatsForDateRange(
      @Param("previousStart") Instant previousStart,
      @Param("previousEnd") Instant previousEnd,
      @Param("currentStart") Instant currentStart,
      @Param("currentEnd") Instant currentEnd);

  // Find order by order number
  Optional<Order> findByOrderNumber(String orderNumber);

  // Check if order number exists
  boolean existsByOrderNumber(String orderNumber);

  // Count orders created today (for order number generation)
  @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.orderDate) = :date")
  Long countOrdersCreatedToday(@Param("date") LocalDate date);
}
