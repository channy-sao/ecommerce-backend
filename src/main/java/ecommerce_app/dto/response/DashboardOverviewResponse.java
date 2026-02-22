package ecommerce_app.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {
  private OrderStatsResponse stats;
  private List<RevenueTrendResponse> revenueTrend;
  private List<StatusDistributionResponse> statusDistribution;
  private List<TopProductResponse> topProducts;
  private List<RecentOrderResponse> recentOrders;
}
