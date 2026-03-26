package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Order statistics response for dashboard overview")
public class OrderStatsResponse {

  @Schema(description = "Total revenue generated", example = "125000.00")
  private BigDecimal totalRevenue;

  @Schema(description = "Total number of orders", example = "500")
  private Long totalOrders;

  @Schema(description = "Number of orders in PENDING status", example = "42")
  private Long pendingOrders;

  @Schema(description = "Number of orders in PROCESSING status", example = "30")
  private Long processingOrders;

  @Schema(description = "Number of orders in SHIPPED status", example = "25")
  private Long shippedOrders;

  @Schema(description = "Number of orders in DELIVERED status", example = "20")
  private Long deliveredOrders;

  @Schema(description = "Number of orders in COMPLETED status", example = "350")
  private Long completedOrders;

  @Schema(description = "Number of orders in CANCELLED status", example = "33")
  private Long cancelledOrders;

  @Schema(description = "Average value per order", example = "250.00")
  private BigDecimal averageOrderValue;

  @Schema(description = "Revenue change percentage compared to previous period", example = "12.5")
  private BigDecimal revenueChangePercent;

  @Schema(
      description = "Orders count change percentage compared to previous period",
      example = "8.3")
  private BigDecimal ordersChangePercent;

  @Schema(
      description = "Pending orders change percentage compared to previous period",
      example = "-5.0")
  private BigDecimal pendingOrdersChangePercent;

  @Schema(
      description = "Completed orders change percentage compared to previous period",
      example = "15.2")
  private BigDecimal completedOrdersChangePercent;

  @Schema(
      description = "Cancelled orders change percentage compared to previous period",
      example = "-2.1")
  private BigDecimal cancelledOrdersChangePercent;

  @Schema(
      description = "Average order value change percentage compared to previous period",
      example = "3.7")
  private BigDecimal avgOrderValueChangePercent;

  @Schema(description = "Daily order and revenue stats for chart rendering")
  private List<DailyStats> dailyStats;

  public String getFormattedTotalRevenue() {
    if (totalRevenue == null) return "$0";
    return "$" + totalRevenue.setScale(0, java.math.RoundingMode.HALF_UP).toString();
  }

  public String getFormattedAvgOrderValue() {
    if (averageOrderValue == null) return "$0";
    return "$" + averageOrderValue.setScale(2, java.math.RoundingMode.HALF_UP).toString();
  }
}
