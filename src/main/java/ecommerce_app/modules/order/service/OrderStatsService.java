package ecommerce_app.modules.order.service;

import ecommerce_app.modules.order.model.dto.OrderStatsResponse;
import ecommerce_app.modules.order.model.dto.RecentOrderResponse;
import ecommerce_app.modules.order.model.dto.RevenueTrendResponse;
import ecommerce_app.modules.order.model.dto.StatusDistributionResponse;
import ecommerce_app.modules.order.model.dto.TopProductResponse;
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
