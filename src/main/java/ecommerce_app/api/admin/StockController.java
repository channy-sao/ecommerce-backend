package ecommerce_app.api.admin;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.stock.model.dto.ProductImportFilterRequest;
import ecommerce_app.modules.stock.model.dto.ProductImportRequest;
import ecommerce_app.modules.stock.model.dto.ProductImportResponse;
import ecommerce_app.modules.stock.model.dto.UpdateStockRequest;
import ecommerce_app.modules.stock.service.ProductImportService;
import ecommerce_app.modules.stock.service.StockService;
import io.swagger.v3.oas.annotations.Parameter;
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

  private final ProductImportService productImportService;

  // -----------------------------
  // 1) Import product (Create)
  // -----------------------------

  @PostMapping("/import-product")
  public ResponseEntity<BaseBodyResponse> importProducts(
      @RequestBody @Valid ProductImportRequest productImportRequest) {
    this.productImportService.importProduct(productImportRequest);
    return BaseBodyResponse.success(null, "Import Product successful");
  }

  // -----------------------------
  // 2) Update import record (Fix mistakes)
  // -----------------------------

  @PutMapping("/import-product/{id}")
  public ResponseEntity<BaseBodyResponse> updateImportProduct(
      @PathVariable(value = "id") Long id,
      @RequestBody @Valid ProductImportRequest productImportRequest) {
    this.productImportService.updateImportProduct(id, productImportRequest);
    return BaseBodyResponse.success(null, "Update Import Product successful");
  }

  // -----------------------------
  // 3) Get all import history
  // -----------------------------

  @PostMapping("/import-product/filter")
  public ResponseEntity<BaseBodyResponse> getImported(
      @RequestBody @Valid
          ProductImportFilterRequest filterRequest) {
    return BaseBodyResponse.pageSuccess(
        this.productImportService.getImportListing(filterRequest),
        "Get Product Imports successful");
  }

  // -----------------------------
  // 4) Get import history by product
  // -----------------------------
  @GetMapping("/import-product/product/{productId}")
  public ResponseEntity<BaseBodyResponse> getImportedByProduct(
      @PathVariable(value = "productId") Long productId) {
    final List<ProductImportResponse> productImports =
        this.productImportService.getProductImportsByProductId(productId);
    return BaseBodyResponse.success(productImports, "Get Product Import by Product Id successful");
  }

  // -----------------------------
  // 5) Get stock by product
  // -----------------------------
  @GetMapping("/product/{productId}")
  public ResponseEntity<BaseBodyResponse> getStockByProduct(@PathVariable Long productId) {
    return BaseBodyResponse.success(
        stockService.getByProductId(productId), "Get Stock By Product Id successful");
  }

  // -----------------------------
  // 6) Update stock manually (optional)
  // For example: admin adjusts stock
  // -----------------------------
  @PatchMapping("/product/{productId}/adjust")
  public ResponseEntity<BaseBodyResponse> updateStock(
      @PathVariable Long productId, @Valid @RequestBody UpdateStockRequest request) {

    stockService.adjustStock(productId, request.getQuantity());
    return BaseBodyResponse.success(null, "adjust Stock successful");
  }

  // -----------------------------
  // 7) Listing stocks
  // -----------------------------
  @GetMapping
  public ResponseEntity<BaseBodyResponse> getStocks() {
    return BaseBodyResponse.success(stockService.getStocks(), "Listing stocks successful");
  }

  // -----------------------------
  // 8) Get History of imported product by product id
  // -----------------------------
  @GetMapping("/import-product/product/{productId}/history")
  public ResponseEntity<BaseBodyResponse> getProductImportHistory(
      @PathVariable(value = "productId") Long productId) {
    final var productImports =
        this.productImportService.getProductImportHistoryByProductId(productId);
    return BaseBodyResponse.success(
        productImports, "Get Product Import history by Product successful");
  }
}
