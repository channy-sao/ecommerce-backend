package ecommerce_app.modules.product.service.impl;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.mapper.ProductMobileMapper;
import ecommerce_app.modules.product.model.dto.MobileProductFilter;
import ecommerce_app.modules.product.model.dto.MobileProductResponse;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.product.service.ProductMobileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductMobileServiceImpl implements ProductMobileService {
  private final ProductRepository productRepository;
  private final ProductMobileMapper productMobileMapper;

  @Override
  public MobileProductResponse getMobileProductResponse(UUID uuid) {
    var product =
        productRepository
            .findByUuid(uuid)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    return productMobileMapper.toProductMobileResponse(product);
  }

  @Override
  public Page<MobileProductResponse> getMobileProducts(
      MobileProductFilter filter, Pageable pageable) {
    return null;
  }

  @Override
  public boolean isInStock(Long productId) {
    log.info("In stock of {}", productId);
    var product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    return product.getInStock();
  }

  @Override
  public Integer getAvailableStock(Long productId) {
    log.info("get available product {}", productId);
    var product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

    return product.getStockQuantity();
  }

  @Override
  public boolean hasActivePromotion(Long productId) {
    log.info("have active promotion {}", productId);
    var product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

    return product.getHasPromotion();
  }

  @Override
  public List<Product> findProductsWithPromotions(int limit) {
    return List.of();
  }

  @Override
  public List<Product> searchMobileProducts(String keyword, int limit) {
    return List.of();
  }

  @Override
  public Page<Product> searchMobileProducts(String keyword, Pageable pageable) {
    return null;
  }
}
