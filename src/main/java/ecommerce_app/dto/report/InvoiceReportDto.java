package ecommerce_app.dto.report;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.ShippingMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO used to populate the Invoice.jrxml JasperReport. Mapped from Order entity by ReportMapper.
 */
@Data
@Builder
public class InvoiceReportDto {

  // ── Company (hardcoded for now, wire from config later) ──────────────
  private String companyName;
  private String companyLogo;
  private String companyAddress;
  private String companyPhone;
  private String companyEmail;

  // ── Order info ───────────────────────────────────────────────────────
  private String orderNumber;
  private LocalDateTime orderDate;
  private OrderStatus orderStatus;
  private PaymentMethod paymentMethod;
  private ShippingMethod shippingMethod;
  private String notes;
  private String promotionCode;
  private String couponCode;

  // ── Customer info (from order.getUser()) ─────────────────────────────
  private String customerName;
  private String customerEmail;
  private String customerPhone;

  // ── Shipping ─────────────────────────────────────────────────────────
  private String shippingAddressSnapshot;

  // ── Line items ───────────────────────────────────────────────────────
  private List<ReportLineItem> items;

  // ── Totals ───────────────────────────────────────────────────────────
  private BigDecimal subtotalAmount; // sum of all item subtotals before discount
  private BigDecimal discountAmount; // item-level + order-level discounts
  private BigDecimal couponDiscount; // coupon-specific discount
  private BigDecimal shippingCost; // original shipping cost
  private BigDecimal shippingDiscount; // shipping promotion discount
  private BigDecimal totalAmount; // final amount to pay
}
