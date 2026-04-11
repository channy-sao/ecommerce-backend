package ecommerce_app.controller.admin;

import ecommerce_app.service.impl.ReportServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/v1/reports")
@RequiredArgsConstructor
@Tag(
    name = "Report Controller",
    description = "For user can generate Invoice and Receipt of an order")
public class ReportController {

  private final ReportServiceImpl reportServiceImpl;

  // ────────────────────────────────────────────────────────────────────
  // Invoice endpoints
  // ────────────────────────────────────────────────────────────────────

  /**
   * GET /api/reports/invoice/{orderId}/pdf Downloads the invoice as a PDF file. Accessible by the
   * order owner or admin/staff.
   */
  @GetMapping("/invoice/{orderId}/pdf")
  @PreAuthorize("hasAnyAuthority('REPORT_CREATE', 'REPORT_READ')")
  public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long orderId) {
    log.info("Generating invoice PDF for order #{}", orderId);

    byte[] pdf = reportServiceImpl.generateInvoicePdf(orderId);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + orderId + ".pdf\"")
        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
        .body(pdf);
  }

  /**
   * GET /api/reports/invoice/{orderId}/html Returns the invoice as an HTML string (for browser
   * preview).
   */
  @GetMapping(value = "/invoice/{orderId}/html", produces = MediaType.TEXT_HTML_VALUE)
  @PreAuthorize("hasAnyAuthority('REPORT_CREATE', 'REPORT_READ')")
  public ResponseEntity<String> previewInvoiceHtml(@PathVariable Long orderId) {
    log.info("Generating invoice HTML preview for order #{}", orderId);

    String html = reportServiceImpl.generateInvoiceHtml(orderId);

    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
  }

  // ────────────────────────────────────────────────────────────────────
  // Receipt endpoints
  // ────────────────────────────────────────────────────────────────────

  /**
   * GET /api/reports/receipt/{orderId}/pdf Downloads the receipt as a PDF file. Only available
   * after payment is confirmed (PaymentTransaction must exist).
   */
  @GetMapping("/receipt/{orderId}/pdf")
  @PreAuthorize("hasAnyAuthority('REPORT_CREATE', 'REPORT_READ')")
  public ResponseEntity<byte[]> downloadReceiptPdf(@PathVariable Long orderId) {
    log.info("Generating receipt PDF for order #{}", orderId);

    byte[] pdf = reportServiceImpl.generateReceiptPdf(orderId);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"receipt-" + orderId + ".pdf\"")
        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
        .body(pdf);
  }

  /**
   * GET /api/reports/receipt/{orderId}/html Returns the receipt as an HTML string (for browser
   * preview).
   */
  @GetMapping(value = "/receipt/{orderId}/html", produces = MediaType.TEXT_HTML_VALUE)
  @PreAuthorize("hasAnyAuthority('REPORT_CREATE', 'REPORT_READ')")
  public ResponseEntity<String> previewReceiptHtml(@PathVariable Long orderId) {
    log.info("Generating receipt HTML preview for order #{}", orderId);

    String html = reportServiceImpl.generateReceiptHtml(orderId);

    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
  }
}
