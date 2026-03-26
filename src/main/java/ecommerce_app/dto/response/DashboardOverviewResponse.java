package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dashboard overview response containing aggregated store statistics")
public class DashboardOverviewResponse {

  @Schema(description = "Overall order and revenue statistics")
  private OrderStatsResponse stats;

  @Schema(description = "Revenue trend data over a time period")
  private List<RevenueTrendResponse> revenueTrend;

  @Schema(description = "Distribution of orders by status")
  private List<StatusDistributionResponse> statusDistribution;

  @Schema(description = "Top performing products by sales or revenue")
  private List<TopProductResponse> topProducts;

  @Schema(description = "Most recent orders placed in the store")
  private List<RecentOrderResponse> recentOrders;
}
