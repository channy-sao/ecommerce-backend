package ecommerce_app.modules.order.model.dto;

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
