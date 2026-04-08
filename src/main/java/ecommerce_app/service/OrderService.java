package ecommerce_app.service;

import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.dto.request.CheckoutRequest;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;
import org.springframework.data.domain.Page;

public interface OrderService {
  OrderResponse checkout(CheckoutRequest checkoutRequest, Long userId);
  void cancelOrder(Long orderId, Long userId, String reason);

  Page<OrderResponse> getOrders(Long userId, int page, int pageSize);

  OrderDetailResponse getOrderDetails(Long orderId, Long userId);
}
