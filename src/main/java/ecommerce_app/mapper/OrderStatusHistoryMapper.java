package ecommerce_app.mapper;

import ecommerce_app.dto.response.OrderStatusHistoryResponse;
import ecommerce_app.entity.OrderStatusHistory;
import ecommerce_app.util.AuditUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusHistoryMapper {
  private final AuditUserResolver auditUserResolver;

  public OrderStatusHistoryResponse toResponse(OrderStatusHistory entity) {
    return OrderStatusHistoryResponse.builder()
        .id(entity.getId())
        .status(entity.getStatus())
        .createdAt(entity.getCreatedAt())
        .createdBy(auditUserResolver.resolve(entity.getCreatedBy()))
        .build();
  }
}
