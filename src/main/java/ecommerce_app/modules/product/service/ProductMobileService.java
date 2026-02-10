package ecommerce_app.modules.product.service;

import ecommerce_app.modules.product.model.dto.MobileProductDetailResponse;
import ecommerce_app.modules.product.model.dto.MobileProductFilter;
import ecommerce_app.modules.product.model.dto.MobileProductResponse;
import ecommerce_app.modules.product.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductMobileService {

  // Mobile response methods
  MobileProductResponse getMobileProductResponse(UUID uuid);

  Page<MobileProductResponse> getMobileProducts(MobileProductFilter filter, Pageable pageable);

  // Stock management
  boolean isInStock(Long productId);

  Integer getAvailableStock(Long productId);

  // Promotions
  boolean hasActivePromotion(Long productId);

  List<Product> findProductsWithPromotions(int limit);

  // Search
  List<Product> searchMobileProducts(String keyword, int limit);

  Page<Product> searchMobileProducts(String keyword, Pageable pageable);
}
