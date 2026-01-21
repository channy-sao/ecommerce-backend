package ecommerce_app.infrastructure.mapper;

import ecommerce_app.core.SimpleTry;
import ecommerce_app.infrastructure.exception.SimpleTryException;
import ecommerce_app.modules.address.model.dto.AddressResponse;
import ecommerce_app.modules.order.model.dto.*;
import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.util.JsonUtils;
import ecommerce_app.util.ProductMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {
  private final UserOrderMapper userMapper;

  // For order details
  public static OrderDetailResponse toDetailResponse(Order order) {
    // Parse address from snapshot
    AddressResponse addressInfo = null;
    if (order.getShippingAddressSnapshot() != null) {
      addressInfo = parseAddressSnapshot(order.getShippingAddressSnapshot());
    }

    // Map items
    List<OrderDetailResponse.OrderItemDetail> items = null;
    if (order.getOrderItems() != null) {
      items = order.getOrderItems().stream().map(OrderMapper::toItemDetail).toList();
    }

    // Build financial breakdown
    OrderDetailResponse.FinancialBreakdown financialBreakdown =
        OrderDetailResponse.FinancialBreakdown.builder()
            .subtotal(order.getSubtotalAmount())
            .discount(order.getDiscountAmount())
            .shipping(order.getShippingCost())
            .total(order.getTotalAmount())
            .build();

    return OrderDetailResponse.builder()
        .id(order.getId())
        .orderNumber(generateOrderNumber(order))
        .orderDate(order.getOrderDate())
        .orderStatus(order.getOrderStatus())
        .paymentStatus(order.getPaymentStatus())
        .paymentMethod(order.getPaymentMethod())
        .shippingMethod(order.getShippingMethod())
        .shippingCost(order.getShippingCost())
        .financialBreakdown(financialBreakdown)
        .items(items)
        .shippingAddress(addressInfo)
        .build();
  }

  private static OrderResponse.OrderItemSimple toItemSimple(OrderItem orderItem) {
    return OrderResponse.OrderItemSimple.builder()
        .product(ProductMapper.toProductResponse(orderItem.getProduct()))
        .quantity(orderItem.getQuantity())
        .price(orderItem.getOriginalPrice())
        .total(orderItem.getTotalPrice())
        .build();
  }

  private static OrderDetailResponse.OrderItemDetail toItemDetail(OrderItem orderItem) {
    return OrderDetailResponse.OrderItemDetail.builder()
        .id(orderItem.getId())
        .product(ProductMapper.toProductResponse(orderItem.getProduct()))
        .unitPrice(orderItem.getOriginalPrice())
        .quantity(orderItem.getQuantity())
        .subtotal(orderItem.getSubtotal())
        .discount(orderItem.getDiscountAmount())
        .total(orderItem.getTotalPrice())
        .build();
  }

  private static AddressResponse parseAddressSnapshot(String snapshot) {
    // Simple parsing - you might need to adjust based on your address structure
    return SimpleTry.ofReThrowChecked(
        () -> JsonUtils.fromJson(snapshot, AddressResponse.class), SimpleTryException::new);
  }

  private static String generateOrderNumber(Order order) {
    // Simple order number: ORD-{id}
    return String.format("ORD-%06d", order.getId());
  }

  // For order list
  public OrderResponse toSimpleResponse(Order order) {
    List<OrderResponse.OrderItemSimple> items = null;
    if (order.getOrderItems() != null) {
      items = order.getOrderItems().stream().map(OrderMapper::toItemSimple).toList();
    }

    return OrderResponse.builder()
        .id(order.getId())
        .orderNumber(generateOrderNumber(order))
        .user(userMapper.toUserOrder(order.getUser()))
        .orderDate(order.getOrderDate())
        .orderStatus(order.getOrderStatus())
        .paymentStatus(order.getPaymentStatus())
        .totalAmount(order.getTotalAmount())
        .itemCount(order.getOrderItems() != null ? order.getOrderItems().size() : 0)
        .items(items)
        .build();
  }

  // After checkout - return confirmation
  public OrderResponse toCheckoutResponse(Order order) {
    return OrderResponse.builder()
        .id(order.getId())
        .orderNumber(generateOrderNumber(order))
        .user(userMapper.toUserOrder(order.getUser()))
        .orderDate(order.getOrderDate())
        .orderStatus(order.getOrderStatus())
        .paymentStatus(order.getPaymentStatus())
        .paymentMethod(order.getPaymentMethod())
        .shippingMethod(order.getShippingMethod())
        .subtotalAmount(order.getSubtotalAmount())
        .discountAmount(order.getDiscountAmount())
        .shippingCost(order.getShippingCost())
        .shippingDiscount(order.getShippingDiscount())
        .totalAmount(order.getTotalAmount())
        .promotionCode(order.getPromotionCode())
        .itemCount(order.getOrderItems() != null ? order.getOrderItems().size() : 0)
        .message("Order placed successfully! Order #" + generateOrderNumber(order))
        .build();
  }
}
