package ecommerce_app.modules.order.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStats {
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate date;

  private Long orderCount;
  private BigDecimal totalRevenue;
}
