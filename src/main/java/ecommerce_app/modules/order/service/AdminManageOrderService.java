package ecommerce_app.modules.order.service;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.modules.order.model.dto.OrderDetailResponse;
import ecommerce_app.modules.order.model.dto.OrderResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface AdminManageOrderService {
  void updateOrderStatus(Long orderId, OrderStatus newStatus);

  Page<OrderResponse> adminGetOrders(
      OrderStatus orderStatus,
      PaymentStatus paymentStatus,
      LocalDate fromDate,
      LocalDate toDate,
      boolean isPaged,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction sortDirection);
    OrderDetailResponse getOrderDetailForAdmin(Long orderId);
}
