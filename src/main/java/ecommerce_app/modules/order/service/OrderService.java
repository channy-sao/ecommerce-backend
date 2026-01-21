package ecommerce_app.modules.order.service;

import ecommerce_app.modules.order.model.dto.CheckoutRequest;
import ecommerce_app.modules.order.model.dto.OrderDetailResponse;
import ecommerce_app.modules.order.model.dto.OrderResponse;

import java.util.List;

public interface OrderService {
  OrderResponse checkout(CheckoutRequest checkoutRequest, Long userId);

  List<OrderResponse> getOrders(Long userId);

  OrderDetailResponse getOrderDetails(Long orderId, Long userId);
}
