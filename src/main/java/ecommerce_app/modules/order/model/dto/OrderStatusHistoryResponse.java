package ecommerce_app.modules.order.model.dto;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.infrastructure.model.response.AuditUserDto;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderStatusHistoryResponse {
  private Long id;
  private OrderStatus status;
  private Instant createdAt;
  private AuditUserDto createdBy;
}
