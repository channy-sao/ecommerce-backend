package ecommerce_app.modules.order.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderResponse {
  private Long id;
  private String orderNumber;
  private String customer;
  private LocalDate date;
  private BigDecimal amount;
  private String status;
  private Integer items;
}
