package ecommerce_app.controller.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.response.*;
import ecommerce_app.service.StockManagementService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/stock")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Stock Management", description = "Inventory operations & monitoring")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
public class StockManagementController {

  private final StockManagementService stockManagementService;
  private final MessageSourceService messageSourceService;

  @Operation(summary = "Get stock summary for a product")
  @GetMapping("/products/{productId}")
  public ResponseEntity<BaseBodyResponse<ProductStockResponse>> getProductStock(
      @PathVariable Long productId) {
    return BaseBodyResponse.success(
        stockManagementService.getProductStock(productId),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @Operation(summary = "Adjust stock (IN, OUT, ADJUSTMENT, RETURN)")
  @PostMapping("/adjust")
  public ResponseEntity<BaseBodyResponse<VariantStockResponse>> adjustStock(
      @RequestBody @Valid StockAdjustmentRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {
    VariantStockResponse response = stockManagementService.adjustStock(request, user.getId());
    return BaseBodyResponse.success(
        response,
        messageSourceService.getMessage(MessageKeyConstant.STOCK_MESSAGE_ADJUSTMENT_SUCCESS));
  }

  @Operation(summary = "Bulk stock adjustments")
  @PostMapping("/adjust/bulk")
  public ResponseEntity<BaseBodyResponse<Void>> bulkAdjust(
      @RequestBody @Valid List<StockAdjustmentRequest> requests,
      @AuthenticationPrincipal CustomUserDetails user) {
    stockManagementService.bulkStockUpdate(requests, user.getId());
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.STOCK_MESSAGE_ADJUSTMENT_SUCCESS));
  }

  @Operation(summary = "View stock movement history")
  @GetMapping("/movements")
  public ResponseEntity<BaseBodyResponse<List<StockHistoryResponse>>> getHistory(
      @RequestParam Long productId,
      @RequestParam(required = false) Long variantId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endDate,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize) {

    LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
    LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
    Pageable pageable =
        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    return BaseBodyResponse.pageSuccess(
        stockManagementService.getStockHistory(productId, variantId, start, end, pageable),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @Operation(summary = "Get low stock alerts")
  @GetMapping("/alerts/low-stock")
  public ResponseEntity<BaseBodyResponse<List<StockAlertResponse>>> getLowStockAlerts(
      @RequestParam(defaultValue = "10") int threshold,
      @RequestParam(required = false) Long productId) {

    List<StockAlertResponse> alerts =
        productId != null
            ? stockManagementService.getLowStockAlertsForProduct(productId, threshold)
            : stockManagementService.getLowStockAlerts(threshold);

    return BaseBodyResponse.success(
        alerts, messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
