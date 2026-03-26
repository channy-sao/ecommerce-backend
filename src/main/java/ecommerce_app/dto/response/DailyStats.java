package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Daily order and revenue statistics")
public class DailyStats {

  @Schema(description = "The date for this statistics entry", example = "2024-01-01")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate date;

  @Schema(description = "Total number of orders placed on this date", example = "42")
  private Long orderCount;

  @Schema(description = "Total revenue generated on this date", example = "1250.00")
  private BigDecimal totalRevenue;
}
