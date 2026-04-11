package ecommerce_app.dto.report;

import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO used to populate the Receipt.jrxml JasperReport.
 * Mapped from PaymentTransaction + Order by ReportMapper.
 */
@Data
@Builder
public class ReceiptReportDto {

    // ── Company (hardcoded for now, wire from config later) ──────────────
    private String companyName;
    private String companyLogo;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;

    // ── Receipt info ─────────────────────────────────────────────────────
    private String receiptNumber;           // PaymentTransaction.referenceNumber
    private LocalDateTime receiptDate;      // PaymentTransaction.transactionDate
    private String cashierName;             // PaymentTransaction.cashierName

    // ── Order info ───────────────────────────────────────────────────────
    private String orderNumber;             // Order.orderNumber
    private PaymentMethod paymentMethod;    // PaymentTransaction.paymentMethod
    private PaymentStatus paymentStatus;    // Order.paymentStatus

    // ── Customer info ────────────────────────────────────────────────────
    private String customerName;            // User.fullName
    private String customerEmail;           // User.email

    // ── Line items ───────────────────────────────────────────────────────
    private List<ReportLineItem> items;

    // ── Totals ───────────────────────────────────────────────────────────
    private BigDecimal subtotalAmount;      // Order.subtotalAmount
    private BigDecimal discountAmount;      // Order.discountAmount
    private BigDecimal couponDiscount;      // Order.couponDiscount
    private BigDecimal shippingCost;        // Order.shippingCost
    private BigDecimal shippingDiscount;    // Order.shippingDiscount
    private BigDecimal totalAmount;         // Order.totalAmount

    // ── Payment detail ───────────────────────────────────────────────────
    private BigDecimal amountPaid;          // Payment.cashReceived (null for non-cash)
    private BigDecimal changeDue;           // Payment.changeAmount (null for non-cash)
    private String currency;               // Payment.currency
}