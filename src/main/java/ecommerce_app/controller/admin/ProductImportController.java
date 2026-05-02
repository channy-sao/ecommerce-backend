package ecommerce_app.controller.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.request.ProductImportFilterRequest;
import ecommerce_app.dto.request.ProductImportRequest;
import ecommerce_app.dto.response.*;
import ecommerce_app.service.ProductImportService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/imports")
@RequiredArgsConstructor
@Tag(name = "Product Import", description = "Supplier stock imports & purchase records")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
public class ProductImportController {

  private final ProductImportService productImportService;
  private final MessageSourceService messageSourceService;

  @Operation(summary = "Import stock from supplier")
  @PostMapping
  public ResponseEntity<BaseBodyResponse<Void>> create(
      @RequestBody @Valid ProductImportRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {
    productImportService.importProduct(request, user.getId());
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SAVE_SUCCESS));
  }

  @Operation(summary = "Update import record")
  @PutMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<Void>> update(
      @PathVariable Long id, @RequestBody @Valid ProductImportRequest request) {
    productImportService.updateImportProduct(id, request);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_UPDATE_SUCCESS));
  }

  @Operation(summary = "Search & filter imports")
  @PostMapping("/search")
  public ResponseEntity<BaseBodyResponse<List<ProductImportResponse>>> search(
      @RequestBody @Valid ProductImportFilterRequest filter) {
    return BaseBodyResponse.pageSuccess(
        productImportService.getImportListing(filter),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @Operation(summary = "Get imports by product")
  @GetMapping("/product/{productId}")
  public ResponseEntity<BaseBodyResponse<List<ProductImportResponse>>> getByProduct(
      @PathVariable Long productId) {
    return BaseBodyResponse.success(
        productImportService.getProductImportsByProductId(productId),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @Operation(summary = "Get import history for product")
  @GetMapping("/product/{productId}/history")
  public ResponseEntity<BaseBodyResponse<ProductImportHistoryByProductResponse>> getHistory(
      @PathVariable Long productId) {
    return BaseBodyResponse.success(
        productImportService.getProductImportHistoryByProductId(productId),
        messageSourceService.getMessage(MessageKeyConstant.STOCK_TITLE_HISTORY));
  }

  @Operation(summary = "Get import by ID")
  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<ProductImportResponse>> getById(@PathVariable Long id) {
    return BaseBodyResponse.success(
            productImportService.getImportById(id),
            messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @Operation(summary = "Delete import record")
  @DeleteMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<Void>> delete(@PathVariable Long id) {
    productImportService.deleteImport(id);
    return BaseBodyResponse.success(
            messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_DELETE_SUCCESS));
  }
}
