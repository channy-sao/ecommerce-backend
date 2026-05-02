package ecommerce_app.service;

import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.response.ProductStockResponse;
import ecommerce_app.dto.response.StockAlertResponse;
import ecommerce_app.dto.response.StockHistoryResponse;
import ecommerce_app.dto.response.StockResponse;
import ecommerce_app.dto.response.VariantStockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

// StockManagementService.java
public interface StockManagementService {

  // Unified stock adjustment (works for both simple and variant products)
  VariantStockResponse adjustStock(StockAdjustmentRequest request, Long userId);

  // Get current stock for a product (shows all variants)
  ProductStockResponse getProductStock(Long productId);

  // Get stock history
  Page<StockHistoryResponse> getStockHistory(
      Long productId,
      Long variantId,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Pageable pageable);

  // Get low stock alerts
  List<StockAlertResponse> getLowStockAlerts(int threshold);

  // Bulk stock update
  void bulkStockUpdate(List<StockAdjustmentRequest> requests, Long userId);

  List<StockAlertResponse> getLowStockAlertsForProduct(Long productId, int threshold);
}
