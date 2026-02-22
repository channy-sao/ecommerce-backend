package ecommerce_app.service;

import ecommerce_app.dto.request.CheckoutRequest;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
  OrderResponse checkout(CheckoutRequest checkoutRequest, Long userId);

  List<OrderResponse> getOrders(Long userId);

  OrderDetailResponse getOrderDetails(Long orderId, Long userId);
}
