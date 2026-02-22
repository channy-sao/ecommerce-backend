package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.OrderStatus;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderStatusHistoryResponse {
  private Long id;
  private OrderStatus status;
  private LocalDateTime createdAt;
  private AuditUserDto createdBy;
}
