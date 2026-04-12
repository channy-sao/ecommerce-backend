package ecommerce_app.mapper;

import ecommerce_app.constant.app.SettingKeys;
import ecommerce_app.dto.report.InvoiceReportDto;
import ecommerce_app.dto.report.ReceiptReportDto;
import ecommerce_app.dto.report.ReportLineItem;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.OrderItem;
import ecommerce_app.entity.Payment;
import ecommerce_app.entity.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps domain entities → report DTOs. Company info is hardcoded for now — replace with @Value /
 * CompanyService later.
 */
@Component
public class ReportMapper {

  // ── Hardcoded company info (replace later) ───────────────────────────
  private static final String COMPANY_NAME = "My E-Commerce Store";

  // ────────────────────────────────────────────────────────────────────
  // Invoice
  // ────────────────────────────────────────────────────────────────────

  /**
   * Maps an Order entity to InvoiceReportDto. Call this when generating an invoice (before or after
   * payment).
   */
  public InvoiceReportDto toInvoiceDto(Order order, Map<String, String> settingMap) {
    return InvoiceReportDto.builder()
        // company
        .companyName(COMPANY_NAME)
        .companyLogo(null)
        .companyAddress(settingMap.getOrDefault(SettingKeys.STORE_ADDRESS, null))
        .companyPhone(settingMap.getOrDefault(SettingKeys.STORE_PHONE, null))
        .companyEmail(settingMap.getOrDefault(SettingKeys.STORE_EMAIL, null))
        // order
        .orderNumber(order.getOrderNumber())
        .orderDate(order.getOrderDate())
        .orderStatus(order.getOrderStatus())
        .paymentMethod(order.getPaymentMethod())
        .shippingMethod(order.getShippingMethod())
        .notes(order.getNotes())
        .promotionCode(order.getPromotionCode())
        .couponCode(order.getCouponCode())
        // customer
        .customerName(order.getUser().getFullName())
        .customerEmail(order.getUser().getEmail())
        .customerPhone(order.getUser().getPhone())
        // shipping
        .shippingAddressSnapshot(order.getShippingAddressSnapshot())
        // items
        .items(toLineItems(order.getOrderItems()))
        // totals
        .subtotalAmount(orZero(order.getSubtotalAmount()))
        .discountAmount(orZero(order.getDiscountAmount()))
        .couponDiscount(orZero(order.getCouponDiscount()))
        .shippingCost(orZero(order.getShippingCost()))
        .shippingDiscount(orZero(order.getShippingDiscount()))
        .totalAmount(orZero(order.getTotalAmount()))
        .build();
  }

  // ────────────────────────────────────────────────────────────────────
  // Receipt
  // ────────────────────────────────────────────────────────────────────

  /**
   * Maps a PaymentTransaction (+ its linked Order and Payment) to ReceiptReportDto. Call this when
   * generating a receipt (after payment confirmed).
   *
   * @param transaction the PaymentTransaction — must have order and payment eagerly loaded
   */
  public ReceiptReportDto toReceiptDto(
      PaymentTransaction transaction, Map<String, String> settingMap) {
    Order order = transaction.getOrder();
    Payment payment = transaction.getPayment();

    return ReceiptReportDto.builder()
        // company
        .companyName(COMPANY_NAME)
        .companyLogo(null)
        .companyAddress(settingMap.getOrDefault(SettingKeys.STORE_ADDRESS, null))
        .companyPhone(settingMap.getOrDefault(SettingKeys.STORE_PHONE, null))
        .companyEmail(settingMap.getOrDefault(SettingKeys.STORE_EMAIL, null))
        // receipt
        .receiptNumber(transaction.getReferenceNumber())
        .receiptDate(transaction.getTransactionDate())
        .cashierName(transaction.getCashierName())
        // order
        .orderNumber(order.getOrderNumber())
        .paymentMethod(transaction.getPaymentMethod())
        .paymentStatus(order.getPaymentStatus())
        // customer
        .customerName(order.getUser().getFullName())
        .customerEmail(order.getUser().getEmail())
        // items
        .items(toLineItems(order.getOrderItems()))
        // totals
        .subtotalAmount(orZero(order.getSubtotalAmount()))
        .discountAmount(orZero(order.getDiscountAmount()))
        .couponDiscount(orZero(order.getCouponDiscount()))
        .shippingCost(orZero(order.getShippingCost()))
        .shippingDiscount(orZero(order.getShippingDiscount()))
        .totalAmount(orZero(order.getTotalAmount()))
        // payment detail (cash only — null for Bakong/online)
        .amountPaid(payment != null ? payment.getCashReceived() : null)
        .changeDue(payment != null ? payment.getChangeAmount() : null)
        .currency(payment != null ? payment.getCurrency() : "USD")
        .build();
  }

  // ────────────────────────────────────────────────────────────────────
  // Shared helpers
  // ────────────────────────────────────────────────────────────────────

  private List<ReportLineItem> toLineItems(List<OrderItem> orderItems) {
    if (orderItems == null) return List.of();
    return orderItems.stream().map(this::toLineItem).collect(Collectors.toList());
  }

  private ReportLineItem toLineItem(OrderItem item) {
    return ReportLineItem.builder()
        .productName(item.getProduct().getName())
        .productCode(item.getProduct().getCode())
        .quantity(item.getQuantity())
        .originalPrice(orZero(item.getOriginalPrice()))
        .subtotal(orZero(item.getSubtotal()))
        .discountAmount(orZero(item.getDiscountAmount()))
        .totalPrice(orZero(item.getTotalPrice()))
        .build();
  }

  private BigDecimal orZero(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }
}
