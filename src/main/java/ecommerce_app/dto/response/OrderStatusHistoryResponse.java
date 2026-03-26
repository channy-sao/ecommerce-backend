package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Order status history entry")
public class OrderStatusHistoryResponse {

  @Schema(description = "Unique history entry ID", example = "1")
  private Long id;

  @Schema(description = "Order status at this point in history", example = "SHIPPED")
  private OrderStatus status;

  @Schema(description = "Date and time this status was set", example = "2024-01-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "User who made this status change")
  private AuditUserDto createdBy;
}
