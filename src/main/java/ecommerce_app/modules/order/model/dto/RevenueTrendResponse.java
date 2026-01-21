package ecommerce_app.modules.order.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendResponse {
  private LocalDate date;
  private BigDecimal revenue;
  private Integer orders;
}
