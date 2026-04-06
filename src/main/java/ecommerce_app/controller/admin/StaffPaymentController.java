package ecommerce_app.controller.admin;

import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.DailyCashSummary;
import ecommerce_app.dto.response.PaymentTransactionResponse;
import ecommerce_app.dto.response.ReceiptItem;
import ecommerce_app.dto.response.ReceiptResponse;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.PaymentTransaction;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.repository.PaymentTransactionRepository;
import ecommerce_app.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
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
  private final PaymentTransactionRepository transactionRepository;

  /**
   * Staff marks COD payment as collected when cash is received at delivery.
   *
   * <p>PATCH /api/staff/v1/payments/orders/{orderId}/cod/mark-paid
   */
  @PatchMapping("/orders/{orderId}/cod/mark-paid")
  @Operation(summary = "Mark COD payment as collected")
  public ResponseEntity<BaseBodyResponse<Void>> markCodPaid(
      @PathVariable Long orderId, @AuthenticationPrincipal CustomUserDetails userDetails) {

    paymentService.markCodPaid(orderId, userDetails.getId());
    return BaseBodyResponse.success("COD payment marked as collected");
  }

  /**
   * Staff marks Cash-in-Shop payment as collected when customer pays at store.
   *
   * <p>PATCH /api/staff/v1/payments/orders/{orderId}/cash-in-shop/mark-paid
   */
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

    log.info("Staff {} requesting daily summary for date: {}", userDetails.getId(), date);

    DailyCashSummary summary = paymentService.getDailyCashSummary(date);
    return BaseBodyResponse.success(summary, "Success");
  }

  @GetMapping("/cashier-summary")
  @Operation(summary = "Get daily cash collection summary for specific cashier")
  public ResponseEntity<BaseBodyResponse<DailyCashSummary>> getCashierDailySummary(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam Long cashierId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    log.info(
        "Staff {} requesting cashier summary for cashier: {} on date: {}",
        userDetails.getId(),
        cashierId,
        date);

    DailyCashSummary summary = paymentService.getDailyCashSummaryByCashier(date, cashierId);
    return BaseBodyResponse.success(summary, "Success");
  }

  @GetMapping("/transaction/order/{orderId}")
  @Operation(summary = "Get payment transaction details for an order")
  public ResponseEntity<BaseBodyResponse<List<PaymentTransactionResponse>>> getTransactionByOrder(
      @PathVariable Long orderId) {

    log.info("Fetching transactions for order: {}", orderId);

    List<PaymentTransaction> transactions = transactionRepository.findByOrderId(orderId);

    if (transactions.isEmpty()) {
      throw new ResourceNotFoundException("No transactions found for order: " + orderId);
    }

    List<PaymentTransactionResponse> response =
        transactions.stream().map(this::toTransactionResponse).collect(Collectors.toList());

    return BaseBodyResponse.success(response, "Success");
  }

  @GetMapping("/transaction/receipt/{receiptNumber}")
  @Operation(summary = "Get payment transaction by receipt number")
  public ResponseEntity<BaseBodyResponse<PaymentTransactionResponse>> getTransactionByReceipt(
      @PathVariable String receiptNumber) {

    log.info("Fetching transaction for receipt: {}", receiptNumber);

    PaymentTransaction transaction =
        transactionRepository
            .findByReferenceNumber(receiptNumber)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No transaction found for receipt: " + receiptNumber));

    return BaseBodyResponse.success(toTransactionResponse(transaction), "Success");
  }

  @GetMapping("/receipt/{orderId}")
  @Operation(summary = "Generate receipt for cash payment")
  public ResponseEntity<BaseBodyResponse<ReceiptResponse>> getReceipt(@PathVariable Long orderId) {

    log.info("Generating receipt for order: {}", orderId);

    List<PaymentTransaction> transactions = transactionRepository.findByOrderId(orderId);

    if (transactions.isEmpty()) {
      throw new ResourceNotFoundException("No transaction found for order: " + orderId);
    }

    // Get the most recent transaction
    PaymentTransaction transaction = transactions.getFirst();
    ReceiptResponse receipt = buildReceiptResponse(transaction);

    return BaseBodyResponse.success(receipt, "Success");
  }

  @GetMapping("/receipts/date-range")
  @Operation(summary = "Get all receipts for a date range")
  public ResponseEntity<BaseBodyResponse<List<ReceiptResponse>>> getReceiptsByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    log.info("Staff {} requesting receipts from {} to {}", userDetails.getId(), startDate, endDate);

    List<PaymentTransaction> transactions =
        transactionRepository.findTransactionsBetween(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

    List<ReceiptResponse> receipts =
        transactions.stream().map(this::buildReceiptResponse).collect(Collectors.toList());

    return BaseBodyResponse.success(receipts, "Success");
  }

  // Private helper methods for mapping
  private PaymentTransactionResponse toTransactionResponse(PaymentTransaction transaction) {
    return PaymentTransactionResponse.builder()
        .id(transaction.getId())
        .referenceNumber(transaction.getReferenceNumber())
        .amount(transaction.getAmount())
        .paymentMethod(transaction.getPaymentMethod().name())
        .transactionType(transaction.getType().name())
        .status(transaction.getStatus().name())
        .cashierName(transaction.getCashierName())
        .transactionDate(transaction.getTransactionDate())
        .notes(transaction.getNotes())
        .orderId(transaction.getOrder().getId())
        .orderNumber(transaction.getOrder().getOrderNumber())
        .build();
  }

  private ReceiptResponse buildReceiptResponse(PaymentTransaction transaction) {
    Order order = transaction.getOrder();

    return ReceiptResponse.builder()
        .receiptNumber(transaction.getReferenceNumber())
        .orderNumber(order.getOrderNumber())
        .date(transaction.getTransactionDate())
        .amount(transaction.getAmount())
        .paymentMethod(transaction.getPaymentMethod())
        .cashierName(transaction.getCashierName())
        .items(
            order.getOrderItems().stream()
                .map(
                    item ->
                        ReceiptItem.builder()
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .price(item.getOriginalPrice())
                            .total(item.getTotalPrice())
                            .build())
                .collect(Collectors.toList()))
        .subtotal(order.getSubtotalAmount())
        .discount(order.getDiscountAmount())
        .shipping(order.getShippingCost())
        .total(order.getTotalAmount())
        .build();
  }
}
