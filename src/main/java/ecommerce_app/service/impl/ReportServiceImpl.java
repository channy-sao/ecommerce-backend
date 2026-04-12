package ecommerce_app.service.impl;

import ecommerce_app.dto.report.InvoiceReportDto;
import ecommerce_app.dto.report.ReceiptReportDto;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.PaymentTransaction;
import ecommerce_app.exception.ReportGenerationException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.ReportMapper;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.repository.PaymentTransactionRepository;
import ecommerce_app.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMMM dd, yyyy");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

  private final OrderRepository orderRepository;
  private final PaymentTransactionRepository transactionRepository;
  private final ReportMapper reportMapper;

  // ────────────────────────────────────────────────────────────────────
  // Invoice
  // ────────────────────────────────────────────────────────────────────

  /** Generate invoice PDF by order ID. */
  public byte[] generateInvoicePdf(Long orderId) {
    InvoiceReportDto dto = buildInvoiceDto(orderId);
    JasperPrint print = fillInvoiceReport(dto);
    return exportToPdf(print);
  }

  /** Generate invoice HTML preview by order ID. */
  public String generateInvoiceHtml(Long orderId) {
    InvoiceReportDto dto = buildInvoiceDto(orderId);
    JasperPrint print = fillInvoiceReport(dto);
    return exportToHtml(print);
  }

  // ────────────────────────────────────────────────────────────────────
  // Receipt
  // ────────────────────────────────────────────────────────────────────

  /**
   * Generate receipt PDF by order ID. Uses the most recent PaymentTransaction linked to the order.
   */
  public byte[] generateReceiptPdf(Long orderId) {
    ReceiptReportDto dto = buildReceiptDto(orderId);
    JasperPrint print = fillReceiptReport(dto);
    return exportToPdf(print);
  }

  /** Generate receipt HTML preview by order ID. */
  public String generateReceiptHtml(Long orderId) {
    ReceiptReportDto dto = buildReceiptDto(orderId);
    JasperPrint print = fillReceiptReport(dto);
    return exportToHtml(print);
  }

  // ────────────────────────────────────────────────────────────────────
  // Build DTOs
  // ────────────────────────────────────────────────────────────────────

  private InvoiceReportDto buildInvoiceDto(Long orderId) {
    Order order =
        orderRepository
            .findByIdWithItemsAndUser(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    return reportMapper.toInvoiceDto(order);
  }

  private ReceiptReportDto buildReceiptDto(Long orderId) {
    // Find the latest successful transaction for this order
    List<PaymentTransaction> transactions = transactionRepository.findByOrderId(orderId);
    if (transactions.isEmpty()) {
      log.error("No payment transaction found for order: {}", orderId);
      throw new ResourceNotFoundException("No payment transaction found for order: " + orderId);
    }
    // Use the most recent transaction
    PaymentTransaction transaction = transactions.getFirst();
    return reportMapper.toReceiptDto(transaction);
  }

  // ────────────────────────────────────────────────────────────────────
  // Fill reports
  // ────────────────────────────────────────────────────────────────────

  private JasperPrint fillInvoiceReport(InvoiceReportDto dto) {
    try {
      JasperReport report = compileReport("reports/Invoice.jrxml");
      Map<String, Object> params = buildInvoiceParams(dto);
      JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(dto.getItems());
      return JasperFillManager.fillReport(report, params, ds);
    } catch (JRException e) {
      log.error(e.getMessage(), e);
      throw new ReportGenerationException("Failed to fill invoice report", e);
    }
  }

  private JasperPrint fillReceiptReport(ReceiptReportDto dto) {
    try {
      JasperReport report = compileReport("reports/Receipt.jrxml");
      Map<String, Object> params = buildReceiptParams(dto);
      JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(dto.getItems());
      return JasperFillManager.fillReport(report, params, ds);
    } catch (JRException e) {
      log.error(e.getMessage(), e);
      throw new ReportGenerationException("Failed to fill receipt report", e);
    }
  }

  // ────────────────────────────────────────────────────────────────────
  // Parameter builders
  // ────────────────────────────────────────────────────────────────────

  private Map<String, Object> buildInvoiceParams(InvoiceReportDto dto) {
    Map<String, Object> p = new HashMap<>();

    // Company
    p.put("CompanyName", dto.getCompanyName());
    p.put("CompanyLogo", dto.getCompanyLogo());
    p.put("CompanyAddress", dto.getCompanyAddress());
    p.put("CompanyPhone", dto.getCompanyPhone());
    p.put("CompanyEmail", dto.getCompanyEmail());

    // Order
    p.put("OrderNumber", dto.getOrderNumber());
    p.put("OrderDate", dto.getOrderDate() != null ? dto.getOrderDate().format(DATE_FORMATTER) : "");
    p.put("OrderStatus", dto.getOrderStatus() != null ? dto.getOrderStatus().name() : "");
    p.put("PaymentMethod", dto.getPaymentMethod() != null ? dto.getPaymentMethod().name() : "");
    p.put("ShippingMethod", dto.getShippingMethod() != null ? dto.getShippingMethod().name() : "");
    p.put("Notes", dto.getNotes());
    p.put("PromotionCode", dto.getPromotionCode());
    p.put("CouponCode", dto.getCouponCode());

    // Customer
    p.put("CustomerName", dto.getCustomerName());
    p.put("CustomerEmail", dto.getCustomerEmail());
    p.put("CustomerPhone", dto.getCustomerPhone());

    // Shipping
    p.put("ShippingAddress", dto.getShippingAddressSnapshot());

    // Totals
    p.put("Subtotal", orZero(dto.getSubtotalAmount()));
    p.put("DiscountAmount", orZero(dto.getDiscountAmount()));
    p.put("CouponDiscount", orZero(dto.getCouponDiscount()));
    p.put("ShippingCost", orZero(dto.getShippingCost()));
    p.put("ShippingDiscount", orZero(dto.getShippingDiscount()));
    p.put("Total", orZero(dto.getTotalAmount()));

    return p;
  }

  private Map<String, Object> buildReceiptParams(ReceiptReportDto dto) {
    Map<String, Object> p = new HashMap<>();

    // Company
    p.put("CompanyName", dto.getCompanyName());
    p.put("CompanyLogo", dto.getCompanyLogo());
    p.put("CompanyAddress", dto.getCompanyAddress());
    p.put("CompanyPhone", dto.getCompanyPhone());
    p.put("CompanyEmail", dto.getCompanyEmail());

    // Receipt
    p.put("ReceiptNumber", dto.getReceiptNumber());
    p.put(
        "ReceiptDate",
        dto.getReceiptDate() != null ? dto.getReceiptDate().format(DATE_FORMATTER) : "");
    p.put(
        "ReceiptTime",
        dto.getReceiptDate() != null ? dto.getReceiptDate().format(TIME_FORMATTER) : "");
    p.put("CashierName", dto.getCashierName());

    // Order
    p.put("OrderNumber", dto.getOrderNumber());
    p.put("PaymentMethod", dto.getPaymentMethod() != null ? dto.getPaymentMethod().name() : "");
    p.put("PaymentStatus", dto.getPaymentStatus() != null ? dto.getPaymentStatus().name() : "");
    p.put("PaymentReference", dto.getReceiptNumber());

    // Customer
    p.put("CustomerName", dto.getCustomerName());
    p.put("CustomerEmail", dto.getCustomerEmail());

    // Totals
    p.put("Subtotal", orZero(dto.getSubtotalAmount()));
    p.put("Discount", orZero(dto.getDiscountAmount()));
    p.put("CouponDiscount", orZero(dto.getCouponDiscount()));
    p.put("ShippingCost", orZero(dto.getShippingCost()));
    p.put("ShippingDiscount", orZero(dto.getShippingDiscount()));
    p.put("Total", orZero(dto.getTotalAmount()));
    p.put("AmountPaid", dto.getAmountPaid());
    p.put("ChangeDue", dto.getChangeDue());
    p.put("Currency", dto.getCurrency() != null ? dto.getCurrency() : "USD");

    return p;
  }

  // ────────────────────────────────────────────────────────────────────
  // Export
  // ────────────────────────────────────────────────────────────────────

  private byte[] exportToPdf(JasperPrint print) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      JRPdfExporter exporter = new JRPdfExporter();
      exporter.setExporterInput(new SimpleExporterInput(print));
      exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
      exporter.exportReport();
      return out.toByteArray();
    } catch (JRException e) {
      log.error(e.getMessage(), e);
      throw new ReportGenerationException("Failed to export PDF", e);
    }
  }

  private String exportToHtml(JasperPrint print) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      HtmlExporter exporter = new HtmlExporter();
      exporter.setExporterInput(new SimpleExporterInput(print));
      exporter.setExporterOutput(new SimpleHtmlExporterOutput(out));
      exporter.exportReport();
      return out.toString();
    } catch (JRException e) {
      log.error(e.getMessage(), e);
      throw new ReportGenerationException("Failed to export HTML", e);
    }
  }

  // ────────────────────────────────────────────────────────────────────
  // Helpers
  // ────────────────────────────────────────────────────────────────────

  private JasperReport compileReport(String classpathLocation) throws JRException {
    try {
      ClassPathResource resource = new ClassPathResource(classpathLocation);
      return JasperCompileManager.compileReport(resource.getInputStream());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new JRException("Could not load report: " + classpathLocation, e);
    }
  }

  private BigDecimal orZero(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }
}
