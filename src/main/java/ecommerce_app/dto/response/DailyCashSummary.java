package ecommerce_app.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DailyCashSummary {
  private LocalDate date;
  private BigDecimal totalCodCollected;
  private BigDecimal totalCashInShopCollected;
  private BigDecimal grandTotal;
  private Integer totalCodOrders;
  private Integer totalCashInShopOrders;
  private List<CashierSummary> cashiers;

  @Data
  @Builder
  public static class CashierSummary {
    private Long cashierId;
    private String cashierName;
    private BigDecimal totalCollected;
    private Integer orderCount;
  }
}
