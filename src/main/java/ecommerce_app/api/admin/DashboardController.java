// OrderStatsController.java
package ecommerce_app.api.admin;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.order.model.dto.DashboardOverviewResponse;
import ecommerce_app.modules.order.model.dto.OrderStatsResponse;
import ecommerce_app.modules.order.model.dto.RecentOrderResponse;
import ecommerce_app.modules.order.model.dto.RevenueTrendResponse;
import ecommerce_app.modules.order.model.dto.StatusDistributionResponse;
import ecommerce_app.modules.order.model.dto.TopProductResponse;
import ecommerce_app.modules.order.service.impl.OrderStatsServiceImpl;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/order-stats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Statistics Controller", description = "For admin manage statistics virtualization")
// @PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

  private final OrderStatsServiceImpl orderStatsService;

  /** Get dashboard statistics (last 30 days) GET /api/v1/orders/stats/dashboard */
  @GetMapping("/stats/dashboard")
  public ResponseEntity<BaseBodyResponse> getDashboardStats() {
    log.info("Fetching dashboard statistics");
    OrderStatsResponse stats = orderStatsService.getDashboardStats();
    return BaseBodyResponse.success(stats, "Dashboard statistics fetched successfully");
  }

  /**
   * Get statistics for a specific date range GET
   * /api/v1/orders/stats?fromDate=2024-01-01&toDate=2024-01-31
   */
  @GetMapping("/stats")
  public ResponseEntity<BaseBodyResponse> getOrderStats(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate) {

    OrderStatsResponse stats;
    if (fromDate != null && toDate != null) {
      stats = orderStatsService.getStatsWithDateRange(fromDate, toDate);
    } else {
      // Default to last 30 days
      stats = orderStatsService.getDashboardStats();
    }

    return BaseBodyResponse.success(stats, "Order statistics fetched successfully");
  }

  /** Get today's statistics GET /api/v1/orders/stats/today */
  @GetMapping("/stats/today")
  public ResponseEntity<BaseBodyResponse> getTodayStats() {
    LocalDate today = LocalDate.now();
    OrderStatsResponse stats = orderStatsService.getStatsWithDateRange(today, today);
    return BaseBodyResponse.success(stats, "Today's statistics fetched successfully");
  }

  /** Get this week's statistics GET /api/v1/orders/stats/week */
  @GetMapping("/stats/week")
  public ResponseEntity<BaseBodyResponse> getWeekStats() {
    LocalDate today = LocalDate.now();
    LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday
    OrderStatsResponse stats = orderStatsService.getStatsWithDateRange(weekStart, today);
    return BaseBodyResponse.success(stats, "Week statistics fetched successfully");
  }

  /** Get this month's statistics GET /api/v1/orders/stats/month */
  @GetMapping("/stats/month")
  public ResponseEntity<BaseBodyResponse> getMonthStats() {

    LocalDate today = LocalDate.now();
    LocalDate monthStart = today.withDayOfMonth(1);
    OrderStatsResponse stats = orderStatsService.getStatsWithDateRange(monthStart, today);
    return BaseBodyResponse.success(stats, "Month statistics fetched successfully");
  }

  /** Get revenue trend for dashboard */
  @GetMapping("/revenue-trend")
  public ResponseEntity<BaseBodyResponse> getRevenueTrend(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate) {

    log.info("Fetching revenue trend from {} to {}", fromDate, toDate);

    if (fromDate == null || toDate == null) {
      // Default to last 30 days
      toDate = LocalDate.now();
      fromDate = toDate.minusDays(30);
    }

    List<RevenueTrendResponse> trend = orderStatsService.getRevenueTrend(fromDate, toDate);
    return BaseBodyResponse.success(trend, "Revenue trend fetched successfully");
  }

  /** Get status distribution for dashboard */
  @GetMapping("/status-distribution")
  public ResponseEntity<BaseBodyResponse> getStatusDistribution(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate) {

    log.info("Fetching status distribution from {} to {}", fromDate, toDate);

    if (fromDate == null || toDate == null) {
      // Default to last 30 days
      toDate = LocalDate.now();
      fromDate = toDate.minusDays(30);
    }

    List<StatusDistributionResponse> distribution =
        orderStatsService.getStatusDistribution(fromDate, toDate);
    return BaseBodyResponse.success(distribution, "Status distribution fetched successfully");
  }

  /** Get top products for dashboard */
  @GetMapping("/top-products")
  public ResponseEntity<BaseBodyResponse> getTopProducts(
      @RequestParam(defaultValue = "5") int limit,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate) {

    log.info("Fetching top {} products from {} to {}", limit, fromDate, toDate);

    if (fromDate == null || toDate == null) {
      // Default to last 30 days
      toDate = LocalDate.now();
      fromDate = toDate.minusDays(30);
    }

    List<TopProductResponse> topProducts =
        orderStatsService.getTopProducts(fromDate, toDate, limit);
    return BaseBodyResponse.success(topProducts, "Top products fetched successfully");
  }

  /** Get recent orders for dashboard */
  @GetMapping("/recent-orders")
  public ResponseEntity<BaseBodyResponse> getRecentOrders(
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate) {

    log.info("Fetching recent {} orders from {} to {}", limit, fromDate, toDate);

    if (fromDate == null || toDate == null) {
      // Default to last 30 days
      toDate = LocalDate.now();
      fromDate = toDate.minusDays(30);
    }

    List<RecentOrderResponse> recentOrders =
        orderStatsService.getRecentOrders(fromDate, toDate, limit);
    return BaseBodyResponse.success(recentOrders, "Recent orders fetched successfully");
  }

  /** Get all dashboard data in one call */
  @GetMapping("/dashboard-overview")
  public ResponseEntity<BaseBodyResponse> getDashboardOverview() {
    log.info("Fetching complete dashboard overview");

    // Default to last 30 days
    LocalDate toDate = LocalDate.now();
    LocalDate fromDate = toDate.minusDays(30);

    // Fetch all data
    OrderStatsResponse stats = orderStatsService.getDashboardStats();
    List<RevenueTrendResponse> revenueTrend = orderStatsService.getRevenueTrend(fromDate, toDate);
    List<StatusDistributionResponse> statusDistribution =
        orderStatsService.getStatusDistribution(fromDate, toDate);
    List<TopProductResponse> topProducts = orderStatsService.getTopProducts(fromDate, toDate, 5);
    List<RecentOrderResponse> recentOrders =
        orderStatsService.getRecentOrders(fromDate, toDate, 10);

    // Create dashboard overview DTO
    DashboardOverviewResponse overview =
        DashboardOverviewResponse.builder()
            .stats(stats)
            .revenueTrend(revenueTrend)
            .statusDistribution(statusDistribution)
            .topProducts(topProducts)
            .recentOrders(recentOrders)
            .build();

    return BaseBodyResponse.success(overview, "Dashboard overview fetched successfully");
  }
}
