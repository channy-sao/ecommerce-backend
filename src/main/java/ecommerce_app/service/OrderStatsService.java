package ecommerce_app.service;

import ecommerce_app.dto.response.OrderStatsResponse;
import ecommerce_app.dto.response.RecentOrderResponse;
import ecommerce_app.dto.response.RevenueTrendResponse;
import ecommerce_app.dto.response.StatusDistributionResponse;
import ecommerce_app.dto.response.TopProductResponse;
import java.time.LocalDate;
import java.util.List;

public interface OrderStatsService {
  OrderStatsResponse getDashboardStats();

  OrderStatsResponse getStatsWithDateRange(LocalDate fromDate, LocalDate toDate);

  // Add these new methods
  List<RevenueTrendResponse> getRevenueTrend(LocalDate fromDate, LocalDate toDate);

  List<StatusDistributionResponse> getStatusDistribution(LocalDate fromDate, LocalDate toDate);

  List<TopProductResponse> getTopProducts(LocalDate fromDate, LocalDate toDate, int limit);

  List<RecentOrderResponse> getRecentOrders(LocalDate fromDate, LocalDate toDate, int limit);
}
