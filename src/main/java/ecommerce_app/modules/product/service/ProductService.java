package ecommerce_app.modules.product.service;

import ecommerce_app.modules.product.model.dto.ProductRequest;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface ProductService {
  ProductResponse saveProduct(ProductRequest productRequest);

  ProductResponse updateProduct(ProductRequest productRequest, Long id);

  void deleteProduct(Long id);

  ProductResponse getProductById(Long id);

  List<ProductResponse> getProducts();

  Page<ProductResponse> filter(
      boolean isPage,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction direction,
      Long categoryId,
      String filter);
}
