package ecommerce_app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.dto.request.POSOrderRequest;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;

import java.time.LocalDate;
import java.util.List;

import ecommerce_app.dto.response.POSOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface AdminManageOrderService {
  void updateOrderStatus(Long orderId, OrderStatus newStatus);

  POSOrderResponse createPOSOrder(POSOrderRequest request, Long staffUserId) throws JsonProcessingException;

  POSOrderResponse getPOSOrder(Long orderId);

  List<POSOrderResponse> getTodayPOSOrders(LocalDate date, Long staffUserId);

  Page<OrderResponse> adminGetOrders(
      String orderNumber,
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

  Page<OrderResponse> getOrdersReadyForCollection(
      PaymentMethod paymentMethod, int page, int size, boolean includePaid);
}
