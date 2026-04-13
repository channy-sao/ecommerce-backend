package ecommerce_app.mapper;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.core.SimpleTry;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;
import ecommerce_app.exception.SimpleTryException;
import ecommerce_app.dto.response.AddressResponse;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.OrderItem;
import ecommerce_app.util.JsonUtils;
import ecommerce_app.util.ProductMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {

  private final UserOrderMapper userMapper;

  // ─── Detail response ─────────────────────────────────────────────────────

  public OrderDetailResponse toDetailResponse(Order order) {
    AddressResponse addressInfo = null;
    if (order.getShippingAddressSnapshot() != null) {
      addressInfo = parseAddressSnapshot(order.getShippingAddressSnapshot());
    }

    List<OrderDetailResponse.OrderItemDetail> items = null;
    if (order.getOrderItems() != null) {
      items = order.getOrderItems().stream().map(OrderMapper::toItemDetail).toList();
    }

    OrderDetailResponse.FinancialBreakdown financialBreakdown =
        OrderDetailResponse.FinancialBreakdown.builder()
            .subtotal(order.getSubtotalAmount())
            .discount(order.getDiscountAmount())
            .shipping(order.getShippingCost())
            .total(order.getTotalAmount())
            .build();

    return OrderDetailResponse.builder()
        .id(order.getId())
        .orderNumber(order.getOrderNumber())
        .user(userMapper.toUserOrder(order.getUser()))
        .orderDate(order.getOrderDate())
        .orderStatus(order.getOrderStatus())
        .paymentStatus(order.getPaymentStatus())
        .paymentMethod(order.getPaymentMethod())
        .paymentGateway(resolveGateway(order.getPaymentMethod()))
        .shippingMethod(order.getShippingMethod())
        .shippingCost(order.getShippingCost())
        .financialBreakdown(financialBreakdown)
        .items(items)
        .shippingAddress(addressInfo)
        .build();
  }

  // ─── Simple list response ─────────────────────────────────────────────────

  public OrderResponse toSimpleResponse(Order order) {
    List<OrderResponse.OrderItemSimple> items = null;
    if (order.getOrderItems() != null) {
      items = order.getOrderItems().stream().map(OrderMapper::toItemSimple).toList();
    }

    return OrderResponse.builder()
        .id(order.getId())
        .orderNumber(order.getOrderNumber())
        .user(userMapper.toUserOrder(order.getUser()))
        .orderDate(order.getOrderDate())
        .orderStatus(order.getOrderStatus())
        .paymentStatus(order.getPaymentStatus())
        .paymentMethod(order.getPaymentMethod())
        .paymentGateway(resolveGateway(order.getPaymentMethod())) // ← derives gateway
        .shippingMethod(order.getShippingMethod())
        .subtotalAmount(order.getSubtotalAmount()) // ← was missing
        .discountAmount(order.getDiscountAmount()) // ← was missing
        .shippingCost(order.getShippingCost()) // ← was missing
        .shippingDiscount(order.getShippingDiscount()) // ← was missing
        .totalAmount(order.getTotalAmount())
        .promotionCode(order.getPromotionCode())
        .itemCount(order.getOrderItems() != null ? order.getOrderItems().size() : 0)
        .items(items)
        .build();
  }

  // ─── Checkout response ────────────────────────────────────────────────────

  public OrderResponse toCheckoutResponse(Order order) {
    return OrderResponse.builder()
        .id(order.getId())
        .orderNumber(order.getOrderNumber())
        .user(userMapper.toUserOrder(order.getUser()))
        .orderDate(order.getOrderDate())
        .orderStatus(order.getOrderStatus())
        .paymentStatus(order.getPaymentStatus())
        .paymentMethod(order.getPaymentMethod())
        .paymentGateway(resolveGateway(order.getPaymentMethod()))
        .shippingMethod(order.getShippingMethod())
        .subtotalAmount(order.getSubtotalAmount())
        .discountAmount(order.getDiscountAmount())
        .shippingCost(order.getShippingCost())
        .shippingDiscount(order.getShippingDiscount())
        .totalAmount(order.getTotalAmount())
        .promotionCode(order.getPromotionCode())
        .itemCount(order.getOrderItems() != null ? order.getOrderItems().size() : 0)
        .message("Order placed successfully! Order #" + order.getOrderNumber())
        .build();
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

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
    return SimpleTry.ofReThrowChecked(
        () -> JsonUtils.fromJson(snapshot, AddressResponse.class), SimpleTryException::new);
  }

  /**
   * Safely resolves PaymentGateway from PaymentMethod. Returns null for methods that don't map to a
   * gateway (CASH, CREDIT_CARD, DEBIT).
   */
  private static PaymentGateway resolveGateway(PaymentMethod method) {
    if (method == null) return null;
    return switch (method) {
      case COD -> PaymentGateway.COD;
      case CASH_IN_SHOP -> PaymentGateway.CASH_IN_SHOP;
      case QR_CODE -> PaymentGateway.BAKONG;
      default -> null;
    };
  }
}
