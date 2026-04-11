package ecommerce_app.dto.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Shared line item DTO used by both InvoiceReportDto and ReceiptReportDto. Mapped from OrderItem
 * entity.
 *
 * <p>Field names MUST match the <field> names declared in Invoice.jrxml and Receipt.jrxml because
 * JasperReports uses JRBeanCollectionDataSource reflection to read them.
 */
@Data
@Builder
public class ReportLineItem {

  private String productName; // Product.name
  private String productCode; // Product.code
  private Integer quantity; // OrderItem.quantity
  private BigDecimal originalPrice; // OrderItem.originalPrice  (unit price at time of order)
  private BigDecimal subtotal; // OrderItem.subtotal       (originalPrice × quantity)
  private BigDecimal discountAmount; // OrderItem.discountAmount (discount on this line)
  private BigDecimal totalPrice; // OrderItem.totalPrice     (subtotal − discountAmount)
}
