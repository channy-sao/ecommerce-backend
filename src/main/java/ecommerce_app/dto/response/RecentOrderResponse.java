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
@Schema(description = "Recent order summary for dashboard display")
public class RecentOrderResponse {

  @Schema(description = "Unique order ID", example = "1")
  private Long id;

  @Schema(description = "Human-readable order number", example = "ORD-20240101-0001")
  private String orderNumber;

  @Schema(description = "Full name of the customer who placed the order", example = "John Doe")
  private String customer;

  @Schema(description = "Date the order was placed", example = "2024-01-01")
  private LocalDate date;

  @Schema(description = "Total amount of the order", example = "99.99")
  private BigDecimal amount;

  @Schema(description = "Current status of the order", example = "PENDING")
  private String status;

  @Schema(description = "Total number of items in the order", example = "3")
  private Integer items;
}
