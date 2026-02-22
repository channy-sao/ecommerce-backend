package ecommerce_app.controller.admin;

import ecommerce_app.constant.enums.StockStatus;
import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.request.ProductImportFilterRequest;
import ecommerce_app.dto.response.ProductImportHistoryByProductResponse;
import ecommerce_app.dto.request.ProductImportRequest;
import ecommerce_app.dto.response.ProductImportResponse;
import ecommerce_app.dto.response.StockResponse;
import ecommerce_app.dto.request.UpdateStockRequest;
import ecommerce_app.service.ProductImportService;
import ecommerce_app.service.StockService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/stocks")
@RequiredArgsConstructor
@Tag(name = "Stock Controller", description = "Management Stock of Product")
public class StockController {
  private final StockService stockService;
  private final MessageSourceService messageSourceService;
  private final ProductImportService productImportService;

  // -----------------------------
  // 1) Import product (Create)
  // -----------------------------

  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR') or hasAnyAuthority('STOCK_IMPORT')")
  @PostMapping("/import-product")
  public ResponseEntity<BaseBodyResponse<Void>> importProducts(
      @RequestBody @Valid ProductImportRequest productImportRequest) {
    this.productImportService.importProduct(productImportRequest);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SAVE_SUCCESS));
  }

  // -----------------------------
  // 2) Update import record (Fix mistakes)
  // -----------------------------

  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR') or hasAnyAuthority('STOCK_IMPORT')")
  @PutMapping("/import-product/{id}")
  public ResponseEntity<BaseBodyResponse<Void>> updateImportProduct(
      @PathVariable(value = "id") Long id,
      @RequestBody @Valid ProductImportRequest productImportRequest) {
    this.productImportService.updateImportProduct(id, productImportRequest);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_UPDATE_SUCCESS));
  }

  // -----------------------------
  // 3) Get all import history
  // -----------------------------

  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR') or hasAnyAuthority('STOCK_IMPORT')")
  @PostMapping("/import-product/filter")
  public ResponseEntity<BaseBodyResponse<List<ProductImportResponse>>> getImported(
      @RequestBody @Valid ProductImportFilterRequest filterRequest) {
    return BaseBodyResponse.pageSuccess(
        this.productImportService.getImportListing(filterRequest),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  // -----------------------------
  // 4) Get import history by product
  // -----------------------------
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR') or hasAnyAuthority('STOCK_IMPORT')")
  @GetMapping("/import-product/product/{productId}")
  public ResponseEntity<BaseBodyResponse<List<ProductImportResponse>>> getImportedByProduct(
      @PathVariable(value = "productId") Long productId) {
    final List<ProductImportResponse> productImports =
        this.productImportService.getProductImportsByProductId(productId);
    return BaseBodyResponse.success(
        productImports, messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  // -----------------------------
  // 5) Get stock by product
  // -----------------------------
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR') or hasAnyAuthority('STOCK_IMPORT')")
  @GetMapping("/product/{productId}")
  public ResponseEntity<BaseBodyResponse<StockResponse>> getStockByProduct(
      @PathVariable Long productId) {
    return BaseBodyResponse.success(
        stockService.getByProductId(productId),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  // -----------------------------
  // 6) Update stock manually (optional)
  // For example: admin adjusts stock
  // -----------------------------
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR') or hasAnyAuthority('STOCK_IMPORT', 'STOCK_UPDATE')")
  @PatchMapping("/product/{productId}/adjust")
  public ResponseEntity<BaseBodyResponse<Void>> updateStock(
      @PathVariable Long productId, @Valid @RequestBody UpdateStockRequest request) {

    stockService.adjustStock(productId, request.getQuantity());
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.STOCK_MESSAGE_ADJUSTMENT_SUCCESS));
  }

  // -----------------------------
  // 7) Listing stocks
  // -----------------------------
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<StockResponse>>> getStocks(
      @RequestParam(value = "isPaged", defaultValue = "true") boolean isPaged,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
      @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
      @RequestParam(value = "sortDirection", defaultValue = "DESC") Sort.Direction sortDirection,
      @RequestParam(value = "filter", required = false) String filter,
      @RequestParam(value = "stockStatus", required = false) StockStatus stockStatus) {
    return BaseBodyResponse.pageSuccess(
        stockService.getStocks(isPaged, page, pageSize, sortBy, sortDirection,filter, stockStatus),
        messageSourceService.getMessage(MessageKeyConstant.STOCK_TITLE_LIST));
  }

  // -----------------------------
  // 8) Get History of imported product by product id
  // -----------------------------
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
  @GetMapping("/import-product/product/{productId}/history")
  public ResponseEntity<BaseBodyResponse<ProductImportHistoryByProductResponse>>
      getProductImportHistory(@PathVariable(value = "productId") Long productId) {
    final var productImports =
        this.productImportService.getProductImportHistoryByProductId(productId);
    return BaseBodyResponse.success(
        productImports, messageSourceService.getMessage(MessageKeyConstant.STOCK_TITLE_HISTORY));
  }
}
