package ecommerce_app.service;

import ecommerce_app.dto.request.ProductImportFilterRequest;
import ecommerce_app.dto.response.ProductImportHistoryByProductResponse;
import ecommerce_app.dto.request.ProductImportRequest;
import ecommerce_app.dto.response.ProductImportResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductImportService {
  void importProduct(ProductImportRequest productImportRequest, Long userId);

  void updateImportProduct(Long id, ProductImportRequest productImportRequest);

  List<ProductImportResponse> getProductImports();

  List<ProductImportResponse> getProductImportsByProductId(Long productId);

  ProductImportHistoryByProductResponse getProductImportHistoryByProductId(Long productId);

  Page<ProductImportResponse> getImportListing(ProductImportFilterRequest filter);

  ProductImportResponse getImportById(Long id);
  void deleteImport(Long id);
}
