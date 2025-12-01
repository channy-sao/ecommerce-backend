package ecommerce_app.modules.stock.service;

import ecommerce_app.modules.stock.model.dto.ProductImportRequest;
import ecommerce_app.modules.stock.model.dto.ProductImportResponse;
import java.util.List;

public interface ProductImportService {
  void importProduct(ProductImportRequest productImportRequest);

  void updateImportProduct(Long id, ProductImportRequest productImportRequest);

  List<ProductImportResponse> getProductImports();

  List<ProductImportResponse> getProductImportsByProductId(Long productId);
}
