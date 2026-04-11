package ecommerce_app.service;

/** Service interface for generating reports (invoices and receipts) */
public interface ReportService {

  /**
   * Generate invoice PDF by order ID.
   *
   * @param orderId the order ID
   * @return byte array of the PDF
   */
  byte[] generateInvoicePdf(Long orderId);

  /**
   * Generate invoice HTML preview by order ID.
   *
   * @param orderId the order ID
   * @return HTML string representation of the invoice
   */
  String generateInvoiceHtml(Long orderId);

  /**
   * Generate receipt PDF by order ID. Uses the most recent PaymentTransaction linked to the order.
   *
   * @param orderId the order ID
   * @return byte array of the PDF
   */
  byte[] generateReceiptPdf(Long orderId);

  /**
   * Generate receipt HTML preview by order ID.
   *
   * @param orderId the order ID
   * @return HTML string representation of the receipt
   */
  String generateReceiptHtml(Long orderId);
}
