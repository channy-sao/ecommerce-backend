package ecommerce_app.service;

import ecommerce_app.dto.response.ImportProductFromExcelResponse;
import ecommerce_app.dto.response.NearEmptyStockResponse;
import ecommerce_app.dto.request.ProductRequest;
import ecommerce_app.dto.response.ProductResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
  ProductResponse saveProduct(ProductRequest productRequest);

  ProductResponse updateProduct(ProductRequest productRequest, Long id);

  void deleteProduct(Long id);

  ProductResponse getProductById(Long id);

  ProductResponse getProductByCode(String code);

  List<ProductResponse> getProducts();

  Page<ProductResponse> filter(
      boolean isPage,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction direction,
      Long categoryId,
      Long brandId,
      String filter);

  ImportProductFromExcelResponse importProductFromExcel(MultipartFile file);

  List<NearEmptyStockResponse> getNearEmptyStockProducts();

  long countNearEmptyStockProducts();

  /**
   * Get paginated products by brand for admin. Includes all products regardless of stock status.
   *
   * @param brandId brand ID to filter by
   * @param search optional product name search
   * @param page page number
   * @param size page size
   * @return paginated list of all products for that brand
   */
  Page<ProductResponse> getProductsByBrandForAdmin(Long brandId, String search, int page, int size);
}
