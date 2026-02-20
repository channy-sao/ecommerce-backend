// OrderStatsController.java
package ecommerce_app.api.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.order.model.dto.DashboardOverviewResponse;
import ecommerce_app.modules.order.model.dto.OrderStatsResponse;
import ecommerce_app.modules.order.model.dto.RecentOrderResponse;
import ecommerce_app.modules.order.model.dto.RevenueTrendResponse;
import ecommerce_app.modules.order.model.dto.StatusDistributionResponse;
import ecommerce_app.modules.order.model.dto.TopProductResponse;
import ecommerce_app.modules.order.service.impl.OrderStatsServiceImpl;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/order-stats")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Order Statistics Controller",
    description = "For admin manage statistics virtualization")
// @PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

  private final OrderStatsServiceImpl orderStatsService;
  private final MessageSourceService messageSourceService;

  /** Get dashboard statistics (last 30 days) GET /api/v1/orders/stats/dashboard */
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'SUPER_ADMIN')")
  @GetMapping("/stats/dashboard")
  public ResponseEntity<BaseBodyResponse<OrderStatsResponse>> getDashboardStats() {
    log.info("Fetching dashboard statistics");
    OrderStatsResponse stats = orderStatsService.getDashboardStats();
    return BaseBodyResponse.success(
        stats, messageSourceService.getMessage(MessageKeyConstant.NAV_DASHBOARD));
  }

  /**
   * Get statistics for a specific date range GET
   * /api/v1/orders/stats?fromDate=2024-01-01&toDate=2024-01-31
   */
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'SUPER_ADMIN')")
  @GetMapping("/stats")
  public ResponseEntity<BaseBodyResponse<OrderStatsResponse>> getOrderStats(
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

    return BaseBodyResponse.success(
        stats, messageSourceService.getMessage(MessageKeyConstant.REPORTS_TITLE_REPORTS));
  }

  /** Get today's statistics GET /api/v1/orders/stats/today */
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'SUPER_ADMIN')")
  @GetMapping("/stats/today")
  public ResponseEntity<BaseBodyResponse<OrderStatsResponse>> getTodayStats() {
    LocalDate today = LocalDate.now();
    OrderStatsResponse stats = orderStatsService.getStatsWithDateRange(today, today);
    return BaseBodyResponse.success(
        stats, messageSourceService.getMessage(MessageKeyConstant.REPORTS_PERIOD_TODAY));
  }

  /** Get this week's statistics GET /api/v1/orders/stats/week */
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'SUPER_ADMIN')")
  @GetMapping("/stats/week")
  public ResponseEntity<BaseBodyResponse<OrderStatsResponse>> getWeekStats() {
    LocalDate today = LocalDate.now();
    LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday
    OrderStatsResponse stats = orderStatsService.getStatsWithDateRange(weekStart, today);
    return BaseBodyResponse.success(
        stats, messageSourceService.getMessage(MessageKeyConstant.REPORTS_PERIOD_THIS_WEEK));
  }

  /** Get this month's statistics GET /api/v1/orders/stats/month */
  @GetMapping("/stats/month")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'SUPER_ADMIN')")
  public ResponseEntity<BaseBodyResponse<OrderStatsResponse>> getMonthStats() {

    LocalDate today = LocalDate.now();
    LocalDate monthStart = today.withDayOfMonth(1);
    OrderStatsResponse stats = orderStatsService.getStatsWithDateRange(monthStart, today);
    return BaseBodyResponse.success(
        stats, messageSourceService.getMessage(MessageKeyConstant.REPORTS_PERIOD_THIS_MONTH));
  }

  /** Get revenue trend for dashboard */
  @GetMapping("/revenue-trend")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'SUPER_ADMIN')")
  public ResponseEntity<BaseBodyResponse<List<RevenueTrendResponse>>> getRevenueTrend(
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
    return BaseBodyResponse.success(
        trend, messageSourceService.getMessage(MessageKeyConstant.REPORTS_TITLE_REVENUE_REPORT));
  }

  /** Get status distribution for dashboard */
  @GetMapping("/status-distribution")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'SUPER_ADMIN')")
  public ResponseEntity<BaseBodyResponse<List<StatusDistributionResponse>>> getStatusDistribution(
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
    return BaseBodyResponse.success(
        distribution,
        messageSourceService.getMessage(MessageKeyConstant.REPORTS_LABEL_ORDERS_BY_STATUS));
  }

  /** Get top products for dashboard */
  @GetMapping("/top-products")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'SUPER_ADMIN')")
  public ResponseEntity<BaseBodyResponse<List<TopProductResponse>>> getTopProducts(
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
    return BaseBodyResponse.success(
        topProducts,
        messageSourceService.getMessage(MessageKeyConstant.REPORTS_LABEL_TOP_PRODUCTS));
  }

  /** Get recent orders for dashboard */
  @GetMapping("/recent-orders")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'SUPER_ADMIN')")
  public ResponseEntity<BaseBodyResponse<List<RecentOrderResponse>>> getRecentOrders(
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
    return BaseBodyResponse.success(
        recentOrders, messageSourceService.getMessage(MessageKeyConstant.ORDER_TITLE_LIST));
  }

  /** Get all dashboard data in one call */
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'SUPER_ADMIN')")
  @GetMapping("/dashboard-overview")
  public ResponseEntity<BaseBodyResponse<DashboardOverviewResponse>> getDashboardOverview() {
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

    return BaseBodyResponse.success(
        overview, messageSourceService.getMessage(MessageKeyConstant.NAV_DASHBOARD));
  }
}
