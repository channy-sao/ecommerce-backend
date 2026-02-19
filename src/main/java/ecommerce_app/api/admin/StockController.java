package ecommerce_app.api.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.stock.model.dto.ProductImportFilterRequest;
import ecommerce_app.modules.stock.model.dto.ProductImportHistoryByProductResponse;
import ecommerce_app.modules.stock.model.dto.ProductImportRequest;
import ecommerce_app.modules.stock.model.dto.ProductImportResponse;
import ecommerce_app.modules.stock.model.dto.StockResponse;
import ecommerce_app.modules.stock.model.dto.UpdateStockRequest;
import ecommerce_app.modules.stock.service.ProductImportService;
import ecommerce_app.modules.stock.service.StockService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<StockResponse>>> getStocks() {
    return BaseBodyResponse.success(
        stockService.getStocks(),
        messageSourceService.getMessage(MessageKeyConstant.STOCK_TITLE_LIST));
  }

  // -----------------------------
  // 8) Get History of imported product by product id
  // -----------------------------
  @GetMapping("/import-product/product/{productId}/history")
  public ResponseEntity<BaseBodyResponse<ProductImportHistoryByProductResponse>>
      getProductImportHistory(@PathVariable(value = "productId") Long productId) {
    final var productImports =
        this.productImportService.getProductImportHistoryByProductId(productId);
    return BaseBodyResponse.success(
        productImports, messageSourceService.getMessage(MessageKeyConstant.STOCK_TITLE_HISTORY));
  }
}
