package ecommerce_app.controller.admin;

import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.DailyCashSummary;
import ecommerce_app.dto.response.PaymentTransactionResponse;
import ecommerce_app.dto.response.ReceiptResponse;
import ecommerce_app.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/v1/staff/payments")
@RequiredArgsConstructor
@Tag(name = "Staff Payment Controller", description = "Staff payment management")
public class StaffPaymentController {
  private final PaymentService paymentService;

  @PatchMapping("/orders/{orderId}/cod/mark-paid")
  @Operation(summary = "Mark COD payment as collected")
  public ResponseEntity<BaseBodyResponse<Void>> markCodPaid(
      @PathVariable Long orderId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    paymentService.markCodPaid(orderId, userDetails.getId());
    return BaseBodyResponse.success("COD payment marked as collected");
  }

  @PatchMapping("/orders/{orderId}/cash-in-shop/mark-paid")
  @Operation(summary = "Mark Cash-in-Shop payment as collected")
  public ResponseEntity<BaseBodyResponse<Void>> markCashInShopPaid(
      @PathVariable Long orderId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    paymentService.markCashInShopPaid(orderId, userDetails.getId());
    return BaseBodyResponse.success("Cash-in-Shop payment marked as collected");
  }

  @GetMapping("/daily-summary")
  @Operation(summary = "Get daily cash collection summary")
  public ResponseEntity<BaseBodyResponse<DailyCashSummary>> getDailySummary(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(paymentService.getDailyCashSummary(date), "Success");
  }

  @GetMapping("/cashier-summary")
  @Operation(summary = "Get daily cash collection summary for specific cashier")
  public ResponseEntity<BaseBodyResponse<DailyCashSummary>> getCashierDailySummary(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam Long cashierId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        paymentService.getDailyCashSummaryByCashier(date, cashierId), "Success");
  }

  @GetMapping("/transaction/order/{orderId}")
  @Operation(summary = "Get payment transaction details for an order")
  public ResponseEntity<BaseBodyResponse<List<PaymentTransactionResponse>>> getTransactionByOrder(
      @PathVariable Long orderId) {
    return BaseBodyResponse.success(paymentService.getTransactionsByOrder(orderId), "Success");
  }

  @GetMapping("/transaction/receipt/{receiptNumber}")
  @Operation(summary = "Get payment transaction by receipt number")
  public ResponseEntity<BaseBodyResponse<PaymentTransactionResponse>> getTransactionByReceipt(
      @PathVariable String receiptNumber) {
    return BaseBodyResponse.success(
        paymentService.getTransactionByReceipt(receiptNumber), "Success");
  }

  @GetMapping("/receipt/{orderId}")
  @Operation(summary = "Generate receipt for cash payment")
  public ResponseEntity<BaseBodyResponse<ReceiptResponse>> getReceipt(@PathVariable Long orderId) {
    return BaseBodyResponse.success(paymentService.getReceiptByOrder(orderId), "Success");
  }

  @GetMapping("/receipts/date-range")
  @Operation(summary = "Get all receipts for a date range")
  public ResponseEntity<BaseBodyResponse<List<ReceiptResponse>>> getReceiptsByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        paymentService.getReceiptsByDateRange(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)),
        "Success");
  }
}
