package ecommerce_app.controller.admin;

import ecommerce_app.dto.request.ProductVariantRequest;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.ProductVariantResponse;
import ecommerce_app.entity.VariantStockMovement;
import ecommerce_app.service.ProductVariantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/products/{productId}/variants")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPER_ADMIN')")
@Tag(name = "Product Variant", description = "Manage product variants and stock")
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping
    public ResponseEntity<BaseBodyResponse<List<ProductVariantResponse>>> createVariants(
            @PathVariable Long productId,
            @RequestBody @Valid List<ProductVariantRequest> requests) {
        return BaseBodyResponse.success(variantService.createVariants(productId, requests), "Variants created");
    }

    @GetMapping
    public ResponseEntity<BaseBodyResponse<List<ProductVariantResponse>>> getVariants(
            @PathVariable Long productId) {
        return BaseBodyResponse.success(variantService.getVariantsByProduct(productId), "OK");
    }

    @PatchMapping("/stock")
    public ResponseEntity<BaseBodyResponse<ProductVariantResponse>> adjustStock(
            @PathVariable Long productId,
            @RequestBody @Valid StockAdjustmentRequest request) {
        return BaseBodyResponse.success(variantService.adjustStock(request), "Stock updated");
    }

    @GetMapping("/{variantId}/stock-history")
    public ResponseEntity<BaseBodyResponse<List<VariantStockMovement>>> getStockHistory(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        return BaseBodyResponse.success(variantService.getStockHistory(variantId), "OK");
    }
}