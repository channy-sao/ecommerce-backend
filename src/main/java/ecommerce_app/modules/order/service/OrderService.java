package ecommerce_app.modules.order.service;

import ecommerce_app.modules.order.model.dto.CheckoutRequest;
import ecommerce_app.modules.order.model.entity.Order;

import java.util.List;

public interface OrderService {
    Order checkout(CheckoutRequest checkoutRequest);
    List<Order> getOrders();
}
