package ecommerce_app.service;

import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.request.ProductVariantRequest;
import ecommerce_app.dto.response.ProductVariantResponse;
import ecommerce_app.entity.VariantStockMovement;

import java.util.List;

public interface ProductVariantService {

    List<ProductVariantResponse> createVariants(Long productId, List<ProductVariantRequest> requests);

    ProductVariantResponse updateVariant(Long variantId, ProductVariantRequest request);

    ProductVariantResponse adjustStock(StockAdjustmentRequest request);

    void deductStockForOrder(Long variantId, int quantity, Long orderId);

    void restoreStockForOrder(Long variantId, int quantity, Long orderId);

    List<ProductVariantResponse> getVariantsByProduct(Long productId);

    ProductVariantResponse getVariantById(Long variantId);

    List<VariantStockMovement> getStockHistory(Long variantId);

    void deleteVariant(Long variantId);
}