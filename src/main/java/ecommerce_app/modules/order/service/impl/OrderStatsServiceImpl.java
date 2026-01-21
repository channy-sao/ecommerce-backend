// OrderStatsService.java
package ecommerce_app.modules.order.service.impl;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.infrastructure.io.service.StaticResourceService;
import ecommerce_app.modules.order.model.dto.OrderStatsResponse;
import ecommerce_app.modules.order.model.dto.RecentOrderResponse;
import ecommerce_app.modules.order.model.dto.RevenueTrendResponse;
import ecommerce_app.modules.order.model.dto.StatusDistributionResponse;
import ecommerce_app.modules.order.model.dto.TopProductResponse;
import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.modules.order.repository.OrderRepository;
import ecommerce_app.modules.order.service.OrderStatsService;
import ecommerce_app.modules.product.model.entity.Product;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime thirtyDaysAgo = now.minusDays(30);
    LocalDateTime previousPeriodStart = thirtyDaysAgo.minusDays(30);

    // Current period stats (last 30 days)
    BigDecimal totalRevenue = orderRepository.getTotalRevenueSince(thirtyDaysAgo);
    Long totalOrders = orderRepository.getOrderCountSince(thirtyDaysAgo);
    BigDecimal averageOrderValue =
        totalOrders > 0
            ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    // Get counts by status (last 30 days)
    Long pendingOrders =
        orderRepository.getOrderCountByStatusSince(OrderStatus.PENDING, thirtyDaysAgo);
    Long processingOrders =
        orderRepository.getOrderCountByStatusSince(OrderStatus.PROCESSING, thirtyDaysAgo);
    Long shippedOrders =
        orderRepository.getOrderCountByStatusSince(OrderStatus.SHIPPED, thirtyDaysAgo);
    Long deliveredOrders =
        orderRepository.getOrderCountByStatusSince(OrderStatus.DELIVERED, thirtyDaysAgo);
    Long completedOrders =
        orderRepository.getOrderCountByStatusSince(OrderStatus.COMPLETED, thirtyDaysAgo);
    Long cancelledOrders =
        orderRepository.getOrderCountByStatusSince(OrderStatus.CANCELLED, thirtyDaysAgo);

    // Previous period stats (30-60 days ago)
    BigDecimal previousRevenue =
        orderRepository.getTotalRevenueBetween(previousPeriodStart, thirtyDaysAgo);
    Long previousOrders = orderRepository.getOrderCountBetween(previousPeriodStart, thirtyDaysAgo);
    BigDecimal previousAvgOrderValue =
        previousOrders > 0
            ? previousRevenue.divide(BigDecimal.valueOf(previousOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    // Previous status counts
    Long previousPendingOrders =
        orderRepository.getOrderCountByStatusBetween(
            OrderStatus.PENDING, previousPeriodStart, thirtyDaysAgo);
    Long previousCompletedOrders =
        orderRepository.getOrderCountByStatusBetween(
            OrderStatus.COMPLETED, previousPeriodStart, thirtyDaysAgo);
    Long previousCancelledOrders =
        orderRepository.getOrderCountByStatusBetween(
            OrderStatus.CANCELLED, previousPeriodStart, thirtyDaysAgo);

    // Calculate percentage changes
    BigDecimal revenueChangePercent = calculateChangePercent(previousRevenue, totalRevenue);
    BigDecimal ordersChangePercent =
        calculateChangePercent(BigDecimal.valueOf(previousOrders), BigDecimal.valueOf(totalOrders));
    BigDecimal pendingOrdersChangePercent =
        calculateChangePercent(
            BigDecimal.valueOf(previousPendingOrders != null ? previousPendingOrders : 0),
            BigDecimal.valueOf(pendingOrders != null ? pendingOrders : 0));
    BigDecimal completedOrdersChangePercent =
        calculateChangePercent(
            BigDecimal.valueOf(previousCompletedOrders != null ? previousCompletedOrders : 0),
            BigDecimal.valueOf(completedOrders != null ? completedOrders : 0));
    BigDecimal cancelledOrdersChangePercent =
        calculateChangePercent(
            BigDecimal.valueOf(previousCancelledOrders != null ? previousCancelledOrders : 0),
            BigDecimal.valueOf(cancelledOrders != null ? cancelledOrders : 0));
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
        .pendingOrdersChangePercent(pendingOrdersChangePercent)
        .completedOrdersChangePercent(completedOrdersChangePercent)
        .cancelledOrdersChangePercent(cancelledOrdersChangePercent)
        .avgOrderValueChangePercent(avgOrderValueChangePercent)
        .build();
  }

  @Override
  public OrderStatsResponse getStatsWithDateRange(LocalDate fromDate, LocalDate toDate) {
    LocalDateTime fromDateTime = fromDate.atStartOfDay();
    LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

    // Previous period (same duration before)
    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
    LocalDate previousFromDate = fromDate.minusDays(daysBetween);
    LocalDate previousToDate = fromDate.minusDays(1);

    LocalDateTime previousFromDateTime = previousFromDate.atStartOfDay();
    LocalDateTime previousToDateTime = previousToDate.atTime(23, 59, 59);

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
    var startDateTime = fromDate.atStartOfDay();
    var endDateTime = toDate.atTime(23, 59, 59);
    // Get all orders in date range
    List<Order> orders = orderRepository.findOrdersByDateRange(fromDate, toDate);
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
      LocalDate orderDate = order.getOrderDate().toLocalDate();
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
    List<Order> orders = orderRepository.findOrdersByDateRange(fromDate, toDate);

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
        .sorted((a, b) -> Long.compare(b.getCount(), a.getCount())) // Sort by count descending
        .collect(Collectors.toList());
  }

  @Override
  public List<TopProductResponse> getTopProducts(LocalDate fromDate, LocalDate toDate, int limit) {
    // Get all order items in date range
    List<OrderItem> orderItems = orderRepository.findOrderItemsByDateRange(fromDate, toDate);

    // Filter out cancelled/refunded orders
    orderItems =
        orderItems.stream()
            .filter(oi -> oi.getOrder() != null)
            .filter(
                oi -> {
                  OrderStatus status = oi.getOrder().getOrderStatus();
                  return status != OrderStatus.CANCELLED && status != OrderStatus.REFUNDED;
                })
            .collect(Collectors.toList());

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
      Integer quantity = orderItem.getQuantity() != null ? orderItem.getQuantity() : 0;
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
        .collect(Collectors.toList());
  }

  @Override
  public List<RecentOrderResponse> getRecentOrders(
      LocalDate fromDate, LocalDate toDate, int limit) {
    // Get recent orders with pagination
    List<Order> orders =
        orderRepository.findOrdersByDateRange(
            fromDate, toDate, PageRequest.of(0, limit, Sort.by("orderDate").descending()));

    // Convert to response
    return orders.stream().map(this::convertToRecentOrderResponse).collect(Collectors.toList());
  }

  private RecentOrderResponse convertToRecentOrderResponse(Order order) {
    // Get customer name
    String customerName = "Unknown";
    if (order.getUser() != null) {
      // Assuming User entity has getFullName() or similar method
      customerName = order.getUser().getFullName();
    }

    // Generate order number if not exists
    String orderNumber =
        order.getId() != null ? "ORD-" + String.format("%06d", order.getId()) : "N/A";

    return new RecentOrderResponse(
        order.getId(),
        orderNumber,
        customerName,
        order.getOrderDate().toLocalDate(),
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
