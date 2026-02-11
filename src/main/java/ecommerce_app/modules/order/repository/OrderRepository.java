package ecommerce_app.modules.order.repository;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.modules.order.model.projection.OrderStatsProjection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
  // This avoids the DATE() function that causes type conversion issues
  @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :fromDate AND :toDate")
  List<Order> findOrdersByDateRange(
      @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

  // Get orders by date range with pagination
  @Query(
      "SELECT o FROM Order o WHERE o.orderDate BETWEEN :fromDate AND :toDate "
          + "ORDER BY o.orderDate DESC")
  List<Order> findOrdersByDateRange(
      @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, Pageable pageable);

  // Get order items by date range
  @Query(
      "SELECT oi FROM OrderItem oi "
          + "JOIN oi.order o "
          + "WHERE o.orderDate BETWEEN :fromDate AND :toDate")
  List<OrderItem> findOrderItemsByDateRange(
      @Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

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
      @Param("previousStart") LocalDateTime previousStart, @Param("currentStart") LocalDateTime currentStart);

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
      @Param("previousStart") LocalDateTime previousStart,
      @Param("previousEnd") LocalDateTime previousEnd,
      @Param("currentStart") LocalDateTime currentStart,
      @Param("currentEnd") LocalDateTime currentEnd);

  // Find order by order number
  Optional<Order> findByOrderNumber(String orderNumber);

  // Check if order number exists
  boolean existsByOrderNumber(String orderNumber);

  // Count orders created today (for order number generation)
  @Query("SELECT COUNT(o) FROM Order o WHERE CAST(o.orderDate AS date) = CAST(:date AS date)")
  Long countOrdersCreatedToday(@Param("date") LocalDate date);
}
