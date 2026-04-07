package ecommerce_app.service;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;

import java.time.LocalDate;

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

  Page<OrderResponse> getOrdersReadyForCollection(PaymentMethod paymentMethod, int page, int size);
}
