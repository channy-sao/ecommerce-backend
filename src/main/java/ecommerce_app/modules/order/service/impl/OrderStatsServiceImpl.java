// OrderStatsService.java
package ecommerce_app.modules.order.service.impl;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.infrastructure.exception.InternalServerErrorException;
import ecommerce_app.infrastructure.io.service.StaticResourceService;
import ecommerce_app.modules.order.model.dto.OrderStatsResponse;
import ecommerce_app.modules.order.model.dto.RecentOrderResponse;
import ecommerce_app.modules.order.model.dto.RevenueTrendResponse;
import ecommerce_app.modules.order.model.dto.StatusDistributionResponse;
import ecommerce_app.modules.order.model.dto.TopProductResponse;
import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.modules.order.model.projection.OrderStatsProjection;
import ecommerce_app.modules.order.repository.OrderRepository;
import ecommerce_app.modules.order.service.OrderStatsService;
import ecommerce_app.modules.product.model.entity.Product;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderStatsServiceImpl implements OrderStatsService {
  private final StaticResourceService staticResourceService;
  private final OrderRepository orderRepository;

  @Override
  public OrderStatsResponse getDashboardStats() {
    try {
      Instant now = Instant.now();
      Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
      Instant previousPeriodStart = thirtyDaysAgo.minus(30, ChronoUnit.DAYS);

      // Single database query instead of 15+
      OrderStatsProjection stats =
          orderRepository.getAggregatedDashboardStats(previousPeriodStart, thirtyDaysAgo);

      // Calculate average order values
      BigDecimal averageOrderValue =
          calculateAverage(stats.getCurrentRevenue(), stats.getCurrentOrders());

      BigDecimal previousAvgOrderValue =
          calculateAverage(stats.getPreviousRevenue(), stats.getPreviousOrders());

      // Calculate percentage changes
      BigDecimal revenueChangePercent =
          calculateChangePercent(stats.getPreviousRevenue(), stats.getCurrentRevenue());

      BigDecimal ordersChangePercent =
          calculateChangePercent(
              BigDecimal.valueOf(stats.getPreviousOrders()),
              BigDecimal.valueOf(stats.getCurrentOrders()));

      BigDecimal pendingOrdersChangePercent =
          calculateChangePercent(
              BigDecimal.valueOf(
                  stats.getPreviousPending() != null ? stats.getPreviousPending() : 0),
              BigDecimal.valueOf(
                  stats.getCurrentPending() != null ? stats.getCurrentPending() : 0));

      BigDecimal completedOrdersChangePercent =
          calculateChangePercent(
              BigDecimal.valueOf(
                  stats.getPreviousCompleted() != null ? stats.getPreviousCompleted() : 0),
              BigDecimal.valueOf(
                  stats.getCurrentCompleted() != null ? stats.getCurrentCompleted() : 0));

      BigDecimal cancelledOrdersChangePercent =
          calculateChangePercent(
              BigDecimal.valueOf(
                  stats.getPreviousCancelled() != null ? stats.getPreviousCancelled() : 0),
              BigDecimal.valueOf(
                  stats.getCurrentCancelled() != null ? stats.getCurrentCancelled() : 0));

      BigDecimal avgOrderValueChangePercent =
          calculateChangePercent(previousAvgOrderValue, averageOrderValue);

      return OrderStatsResponse.builder()
          .totalRevenue(stats.getCurrentRevenue())
          .totalOrders(stats.getCurrentOrders())
          .pendingOrders(stats.getCurrentPending())
          .processingOrders(stats.getCurrentProcessing())
          .shippedOrders(stats.getCurrentShipped())
          .deliveredOrders(stats.getCurrentDelivered())
          .completedOrders(stats.getCurrentCompleted())
          .cancelledOrders(stats.getCurrentCancelled())
          .averageOrderValue(averageOrderValue)
          .revenueChangePercent(revenueChangePercent)
          .ordersChangePercent(ordersChangePercent)
          .pendingOrdersChangePercent(pendingOrdersChangePercent)
          .completedOrdersChangePercent(completedOrdersChangePercent)
          .cancelledOrdersChangePercent(cancelledOrdersChangePercent)
          .avgOrderValueChangePercent(avgOrderValueChangePercent)
          .build();
    } catch (Exception e) {
      log.error("Error fetching dashboard stats", e);
      throw new InternalServerErrorException(e.getMessage(), e);
    }
  }

  private BigDecimal calculateAverage(BigDecimal total, Long count) {
    if (count == null || count == 0) {
      return BigDecimal.ZERO;
    }
    return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
  }

  @Override
  public OrderStatsResponse getStatsWithDateRange(LocalDate fromDate, LocalDate toDate) {
    Instant fromDateTime = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant toDateTime = Instant.from(toDate.atTime(23, 59, 59));

    // Previous period (same duration before)
    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
    LocalDate previousFromDate = fromDate.minusDays(daysBetween);
    LocalDate previousToDate = fromDate.minusDays(1);

    Instant previousFromDateTime = Instant.from(previousFromDate.atStartOfDay());
    Instant previousToDateTime = Instant.from(previousToDate.atTime(23, 59, 59));

    // Current period stats
    BigDecimal totalRevenue = orderRepository.getTotalRevenueBetween(fromDateTime, toDateTime);
    Long totalOrders = orderRepository.getOrderCountBetween(fromDateTime, toDateTime);
    BigDecimal averageOrderValue =
        totalOrders > 0
            ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    // Status counts
    Long pendingOrders =
        orderRepository.getOrderCountByStatusBetween(OrderStatus.PENDING, fromDateTime, toDateTime);
    Long processingOrders =
        orderRepository.getOrderCountByStatusBetween(
            OrderStatus.PROCESSING, fromDateTime, toDateTime);
    Long shippedOrders =
        orderRepository.getOrderCountByStatusBetween(OrderStatus.SHIPPED, fromDateTime, toDateTime);
    Long deliveredOrders =
        orderRepository.getOrderCountByStatusBetween(
            OrderStatus.DELIVERED, fromDateTime, toDateTime);
    Long completedOrders =
        orderRepository.getOrderCountByStatusBetween(
            OrderStatus.COMPLETED, fromDateTime, toDateTime);
    Long cancelledOrders =
        orderRepository.getOrderCountByStatusBetween(
            OrderStatus.CANCELLED, fromDateTime, toDateTime);

    // Previous period stats
    BigDecimal previousRevenue =
        orderRepository.getTotalRevenueBetween(previousFromDateTime, previousToDateTime);
    Long previousOrders =
        orderRepository.getOrderCountBetween(previousFromDateTime, previousToDateTime);
    BigDecimal previousAvgOrderValue =
        previousOrders > 0
            ? previousRevenue.divide(BigDecimal.valueOf(previousOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    // Calculate percentage changes
    BigDecimal revenueChangePercent = calculateChangePercent(previousRevenue, totalRevenue);
    BigDecimal ordersChangePercent =
        calculateChangePercent(BigDecimal.valueOf(previousOrders), BigDecimal.valueOf(totalOrders));
    BigDecimal avgOrderValueChangePercent =
        calculateChangePercent(previousAvgOrderValue, averageOrderValue);

    return OrderStatsResponse.builder()
        .totalRevenue(totalRevenue)
        .totalOrders(totalOrders)
        .pendingOrders(pendingOrders)
        .processingOrders(processingOrders)
        .shippedOrders(shippedOrders)
        .deliveredOrders(deliveredOrders)
        .completedOrders(completedOrders)
        .cancelledOrders(cancelledOrders)
        .averageOrderValue(averageOrderValue)
        .revenueChangePercent(revenueChangePercent)
        .ordersChangePercent(ordersChangePercent)
        .avgOrderValueChangePercent(avgOrderValueChangePercent)
        .build();
  }

  @Override
  public List<RevenueTrendResponse> getRevenueTrend(LocalDate fromDate, LocalDate toDate) {
    // Get all orders in date range
    Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant toInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
    List<Order> orders = orderRepository.findOrdersByDateRange(fromInstant, toInstant);
    // Group by date and calculate
    Map<LocalDate, RevenueTrendResponse> trendMap = new TreeMap<>();

    // Initialize all dates in range
    LocalDate currentDate = fromDate;
    while (!currentDate.isAfter(toDate)) {
      trendMap.put(currentDate, new RevenueTrendResponse(currentDate, BigDecimal.ZERO, 0));
      currentDate = currentDate.plusDays(1);
    }

    // Aggregate data
    for (Order order : orders) {
      LocalDate orderDate = order.getOrderDate().atZone(ZoneId.systemDefault()).toLocalDate();
      RevenueTrendResponse trend = trendMap.get(orderDate);

      if (trend != null) {
        // Add revenue
        BigDecimal currentRevenue =
            trend.getRevenue() != null ? trend.getRevenue() : BigDecimal.ZERO;
        BigDecimal orderRevenue =
            order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        trend.setRevenue(currentRevenue.add(orderRevenue));

        // Add order count
        trend.setOrders(trend.getOrders() + 1);
      }
    }

    return new ArrayList<>(trendMap.values());
  }

  @Override
  public List<StatusDistributionResponse> getStatusDistribution(
      LocalDate fromDate, LocalDate toDate) {
    // Get all orders in date range
    Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant toInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

    List<Order> orders = orderRepository.findOrdersByDateRange(fromInstant, toInstant);

    // Group by status
    Map<String, Long> statusCountMap =
        orders.stream()
            .filter(order -> order.getOrderStatus() != null)
            .collect(
                Collectors.groupingBy(
                    order -> order.getOrderStatus().toString(), Collectors.counting()));

    // Calculate total
    long total = statusCountMap.values().stream().mapToLong(Long::longValue).sum();

    // Convert to response
    return statusCountMap.entrySet().stream()
        .map(
            entry -> {
              double percentage = total > 0 ? (entry.getValue().doubleValue() / total) * 100 : 0.0;
              return new StatusDistributionResponse(entry.getKey(), entry.getValue(), percentage);
            })
        .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
        .toList();
  }

  @Override
  public List<TopProductResponse> getTopProducts(LocalDate fromDate, LocalDate toDate, int limit) {
    // Get all order items in date range
    Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant toInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

    List<OrderItem> orderItems = orderRepository.findOrderItemsByDateRange(fromInstant, toInstant);

    // Filter out cancelled/refunded orders
    orderItems =
        orderItems.stream()
            .filter(oi -> oi.getOrder() != null)
            .filter(
                oi -> {
                  OrderStatus status = oi.getOrder().getOrderStatus();
                  return status != OrderStatus.CANCELLED && status != OrderStatus.REFUNDED;
                })
            .toList();

    // Group by product
    Map<Product, ProductStats> productStatsMap = new HashMap<>();

    for (OrderItem orderItem : orderItems) {
      Product product = orderItem.getProduct();
      if (product == null) continue;

      ProductStats stats = productStatsMap.getOrDefault(product, new ProductStats());

      // Add revenue
      BigDecimal itemRevenue =
          orderItem.getTotalPrice() != null ? orderItem.getTotalPrice() : BigDecimal.ZERO;
      stats.revenue = stats.revenue.add(itemRevenue);

      // Add quantity
      int quantity = orderItem.getQuantity() != null ? orderItem.getQuantity() : 0;
      stats.quantity += quantity;

      productStatsMap.put(product, stats);
    }

    // Convert to response and sort by revenue
    return productStatsMap.entrySet().stream()
        .map(
            entry -> {
              Product product = entry.getKey();
              ProductStats stats = entry.getValue();

              return new TopProductResponse(
                  product.getId(),
                  product.getName(),
                  staticResourceService.getProductImageUrl(product.getImage()),
                  stats.revenue,
                  stats.quantity);
            })
        .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue())) // Sort by revenue descending
        .limit(limit)
        .toList();
  }

  @Override
  public List<RecentOrderResponse> getRecentOrders(
      LocalDate fromDate, LocalDate toDate, int limit) {
    // Get recent orders with pagination
    Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant toInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

    List<Order> orders =
        orderRepository.findOrdersByDateRange(
            fromInstant, toInstant, PageRequest.of(0, limit, Sort.by("orderDate").descending()));

    // Convert to response
    return orders.stream().map(this::convertToRecentOrderResponse).toList();
  }

  private RecentOrderResponse convertToRecentOrderResponse(Order order) {
    // Get customer name
    String customerName = "Unknown";
    if (order.getUser() != null) {
      // Assuming User entity has getFullName() or similar method
      customerName = order.getUser().getFullName();
    }

    return new RecentOrderResponse(
        order.getId(),
        order.getOrderNumber(),
        customerName,
        order.getOrderDate().atZone(ZoneId.systemDefault()).toLocalDate(),
        order.getTotalAmount(),
        order.getOrderStatus().toString(),
        order.getOrderItems() != null ? order.getOrderItems().size() : 0);
  }

  private BigDecimal calculateChangePercent(BigDecimal previous, BigDecimal current) {
    if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
      return current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100.00") : BigDecimal.ZERO;
    }

    BigDecimal change =
        current
            .subtract(previous)
            .divide(previous, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));

    return change.setScale(2, RoundingMode.HALF_UP);
  }

  // Helper class for product statistics
  private static class ProductStats {
    BigDecimal revenue = BigDecimal.ZERO;
    int quantity = 0;
  }
}
