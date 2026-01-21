package ecommerce_app.modules.order.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatsResponse {
  // Dashboard Stats (matches your UI)
  private BigDecimal totalRevenue;
  private Long totalOrders;
  private Long pendingOrders;
  private Long processingOrders;
  private Long shippedOrders;
  private Long deliveredOrders;
  private Long completedOrders;
  private Long cancelledOrders;
  private BigDecimal averageOrderValue;

  // Change percentages for UI
  private BigDecimal revenueChangePercent;
  private BigDecimal ordersChangePercent;
  private BigDecimal pendingOrdersChangePercent;
  private BigDecimal completedOrdersChangePercent;
  private BigDecimal cancelledOrdersChangePercent;
  private BigDecimal avgOrderValueChangePercent;

  // For charts (optional)
  private List<DailyStats> dailyStats;

  // Helper method to format for UI
  public String getFormattedTotalRevenue() {
    if (totalRevenue == null) return "$0";
    return "$" + totalRevenue.setScale(0, java.math.RoundingMode.HALF_UP).toString();
  }

  public String getFormattedAvgOrderValue() {
    if (averageOrderValue == null) return "$0";
    return "$" + averageOrderValue.setScale(2, java.math.RoundingMode.HALF_UP).toString();
  }
}
