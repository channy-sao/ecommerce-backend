package ecommerce_app.service;

import ecommerce_app.dto.request.ProductVariantRequest;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.response.ProductVariantResponse;
import ecommerce_app.entity.VariantStockMovement;

import java.util.List;

public interface ProductVariantService {

    // ── Read ─────────────────────────────────────────────────────────
    List<ProductVariantResponse> getVariantsByProduct(Long productId);
    ProductVariantResponse getVariantById(Long variantId);
    List<VariantStockMovement> getStockHistory(Long variantId);

    // ── Write ─────────────────────────────────────────────────────────
    List<ProductVariantResponse> createVariants(Long productId, List<ProductVariantRequest> requests); // bulk
    ProductVariantResponse createVariant(Long productId, ProductVariantRequest request);               // single
    ProductVariantResponse updateVariant(Long variantId, ProductVariantRequest request);
    void deleteVariant(Long variantId);

    // ── Stock ─────────────────────────────────────────────────────────
    ProductVariantResponse adjustStock(StockAdjustmentRequest request);
}