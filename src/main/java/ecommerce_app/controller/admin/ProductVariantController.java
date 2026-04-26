package ecommerce_app.controller.admin;

import ecommerce_app.dto.request.ProductVariantRequest;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.ProductVariantResponse;
import ecommerce_app.entity.VariantStockMovement;
import ecommerce_app.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/products/{productId}/variants")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPER_ADMIN')")
@Tag(name = "Product Variant", description = "Manage product variants and stock")
public class ProductVariantController {

    private final ProductVariantService variantService;

    // ── Bulk create ───────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Bulk-create variants for a product")
    public ResponseEntity<BaseBodyResponse<List<ProductVariantResponse>>> createVariants(
            @PathVariable Long productId,
            @RequestBody @Valid List<ProductVariantRequest> requests) {
        return BaseBodyResponse.success(
            variantService.createVariants(productId, requests), "Variants created");
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all variants of a product")
    public ResponseEntity<BaseBodyResponse<List<ProductVariantResponse>>> getVariants(
            @PathVariable Long productId) {
        return BaseBodyResponse.success(
            variantService.getVariantsByProduct(productId), "OK");
    }

    @GetMapping("/{variantId}")
    @Operation(summary = "Get a single variant by ID")
    public ResponseEntity<BaseBodyResponse<ProductVariantResponse>> getVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        return BaseBodyResponse.success(
            variantService.getVariantById(variantId), "OK");
    }

    @GetMapping("/{variantId}/stock-history")
    @Operation(summary = "Get stock movement history for a variant")
    public ResponseEntity<BaseBodyResponse<List<VariantStockMovement>>> getStockHistory(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        return BaseBodyResponse.success(
            variantService.getStockHistory(variantId), "OK");
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @PutMapping("/{variantId}")
    @Operation(summary = "Update a variant (SKU, price, attributes, threshold)")
    public ResponseEntity<BaseBodyResponse<ProductVariantResponse>> updateVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestBody @Valid ProductVariantRequest request) {
        return BaseBodyResponse.success(
            variantService.updateVariant(variantId, request), "Variant updated");
    }

    // ── Delete (soft) ─────────────────────────────────────────────────────────

    @DeleteMapping("/{variantId}")
    @Operation(summary = "Deactivate a variant (soft delete — preserves order history)")
    public ResponseEntity<BaseBodyResponse<Void>> deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        variantService.deleteVariant(variantId);
        return BaseBodyResponse.success("Variant deactivated");
    }

    // ── Stock ─────────────────────────────────────────────────────────────────

    @PatchMapping("/stock")
    @Operation(summary = "Adjust stock for a variant (IN / OUT / ADJUSTMENT / RETURN)")
    public ResponseEntity<BaseBodyResponse<ProductVariantResponse>> adjustStock(
            @PathVariable Long productId,
            @RequestBody @Valid StockAdjustmentRequest request) {
        return BaseBodyResponse.success(
            variantService.adjustStock(request), "Stock updated");
    }
}