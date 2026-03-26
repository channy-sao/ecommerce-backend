package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Revenue trend data point for chart rendering")
public class RevenueTrendResponse {

  @Schema(description = "Date for this trend data point", example = "2024-01-01")
  private LocalDate date;

  @Schema(description = "Total revenue on this date", example = "1250.00")
  private BigDecimal revenue;

  @Schema(description = "Total number of orders on this date", example = "42")
  private Integer orders;
}
