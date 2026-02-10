package ecommerce_app.modules.product.service.impl;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.mapper.ProductMapper;
import ecommerce_app.modules.product.model.dto.MobileProductListResponse;
import ecommerce_app.modules.product.model.dto.MobileProductResponse;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.product.service.MobileProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Mobile Product Service
 *
 * <p>Dedicated service for mobile app product operations Handles product browsing, searching, and
 * filtering for mobile clients
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MobileProductServiceImpl implements MobileProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  /**
   * Get all products with filters and pagination Main method for product list screen in mobile app
   *
   * @param pageable Pagination and sorting
   * @param categoryId Filter by category (optional)
   * @param isFeature Filter by featured status (optional)
   * @param hasPromotion Filter by promotion status (optional)
   * @param search Search by name or description (optional)
   * @return Paginated list of products
   */
  public Page<MobileProductListResponse> getProducts(
      Pageable pageable, Long categoryId, Boolean isFeature, Boolean hasPromotion, String search) {

    Specification<Product> spec = (root, query, cb) -> cb.conjunction();

    // Filter by category
    if (categoryId != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
    }

    // Filter by featured products
    if (isFeature != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("isFeature"), isFeature));
    }

    // Filter by products with active promotions
    if (hasPromotion != null && hasPromotion) {
      spec =
          spec.and(
              (root, query, cb) -> {
                query.distinct(true);
                return cb.and(
                    cb.isNotEmpty(root.get("promotions")),
                    cb.isTrue(root.join("promotions").get("active")));
              });
    }

    // Search by name or description
    if (search != null && !search.trim().isEmpty()) {
      String searchPattern = "%" + search.toLowerCase() + "%";
      spec =
          spec.and(
              (root, query, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("name")), searchPattern),
                      cb.like(cb.lower(root.get("description")), searchPattern)));
    }

    return productRepository.findAll(spec, pageable).map(productMapper::toListResponse);
  }

  /**
   * Get product by ID Used when user taps on a product card to see full details
   *
   * @param id Product ID
   * @return Product entity with full details
   */
  public MobileProductResponse getProductById(Long id) {
    final var product = getById(id);
    return productMapper.toDetailResponse(product);
  }

  private Product getById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product" + id));
  }

  /**
   * Get product by UUID Used for deep linking and sharing product URLs
   *
   * @param uuid Product UUID
   * @return Product entity
   * @throws ResourceNotFoundException if product not found
   */
  public MobileProductResponse getProductByUuid(UUID uuid) {
    final var product =
        productRepository
            .findByUuid(uuid)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with uuid: " + uuid));
    return productMapper.toDetailResponse(product);
  }

  /**
   * Get featured products Shows highlighted products on homepage
   *
   * @param limit Maximum number of products to return
   * @return List of featured products
   */
  public List<MobileProductListResponse> getFeaturedProducts(int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    return productRepository.findByIsFeatureTrue(pageable).getContent().stream()
        .map(productMapper::toListResponse)
        .toList();
  }

  /**
   * Get products with active promotions Shows products currently on sale
   *
   * @param pageable Pagination and sorting
   * @return Paginated list of products with active promotions
   */
  public Page<MobileProductListResponse> getProductsOnPromotion(Pageable pageable) {
    return productRepository
        .findProductsWithActivePromotions(pageable)
        .map(productMapper::toListResponse);
  }

  /**
   * Get products by category Used for category browsing screen
   *
   * @param categoryId Category ID
   * @param pageable Pagination and sorting
   * @return Paginated list of products in the category
   */
  public Page<MobileProductListResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
    return productRepository
        .findByCategoryId(categoryId, pageable)
        .map(productMapper::toListResponse);
  }

  /**
   * Search products by query Main search functionality for mobile app
   *
   * @param query Search term
   * @param pageable Pagination and sorting
   * @return Paginated list of matching products
   */
  public Page<MobileProductListResponse> searchProducts(String query, Pageable pageable) {
    if (query == null || query.trim().isEmpty()) {
      // Return all products if no search query
      return productRepository.findAll(pageable).map(productMapper::toListResponse);
    }

    return productRepository
        .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable)
        .map(productMapper::toListResponse);
  }

  /**
   * Check if product is in stock Helper method for UI logic
   *
   * @param productId Product ID
   * @return true if product has stock available
   */
  public boolean isProductInStock(Long productId) {
    Product product = getById(productId);
    return product.getInStock();
  }

  /**
   * Get product stock quantity Shows exact quantity to user if needed
   *
   * @param productId Product ID
   * @return Current stock quantity
   */
  public int getProductStockQuantity(Long productId) {
    Product product = getById(productId);
    return product.getStockQuantity();
  }

  /**
   * Check if product can be quick-added to cart Quick add is only available for simple products (no
   * active promotions)
   *
   * @param productId Product ID
   * @return true if product can be quick-added
   */
  public boolean canQuickAddProduct(Long productId) {
    Product product = getById(productId);
    return product.getQuickAddAvailable();
  }

  /**
   * Get related products (same category) Shows "You may also like" section
   *
   * @param productId Current product ID
   * @param limit Maximum number of related products
   * @return List of products in same category
   */
  public List<MobileProductListResponse> getRelatedProducts(Long productId, int limit) {
    Product product = getById(productId);
    Long categoryId = product.getCategory().getId();

    Pageable pageable = PageRequest.of(0, limit + 1); // +1 to exclude current product
    Page<Product> relatedPage = productRepository.findByCategoryId(categoryId, pageable);

    // Filter out the current product
    return relatedPage.getContent().stream()
        .filter(p -> !p.getId().equals(productId))
        .limit(limit)
        .toList().stream().map(productMapper::toListResponse).toList();
  }

  /**
   * Get new arrivals Shows recently added products
   *
   * @param limit Maximum number of products
   * @return List of newest products
   */
  public List<MobileProductListResponse> getNewArrivals(int limit) {
    Pageable pageable =
        PageRequest.of(
            0,
            limit,
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    return productRepository.findAll(pageable).getContent().stream()
        .map(productMapper::toListResponse)
        .toList();
  }

  /**
   * Get popular products (by favorites count) Shows most-liked products
   *
   * @param limit Maximum number of products
   * @return List of most popular products
   */
  public List<MobileProductListResponse> getPopularProducts(int limit) {
    Pageable pageable =
        PageRequest.of(
            0,
            limit,
            Sort.by(
                Sort.Direction.DESC, "favoritesCount"));
    return productRepository.findAll(pageable).getContent().stream()
        .map(productMapper::toListResponse)
        .toList();
  }

  /**
   * Get low stock products Shows "Only X left!" products
   *
   * @param limit Maximum number of products
   * @return List of low stock products
   */
  public List<MobileProductListResponse> getLowStockProducts(int limit) {
    // This would need a custom query in repository
    // For now, get all and filter
    Pageable pageable = PageRequest.of(0, 100);
    final var lowStocks =
        productRepository.findAll(pageable).getContent().stream()
            .filter(p -> "LOW_STOCK".equals(p.getStockStatus()))
            .limit(limit)
            .toList();
    return lowStocks.stream().map(productMapper::toListResponse).toList();
  }

  /**
   * Increment product favorites count Called when user adds product to favorites
   *
   * @param productId Product ID
   */
  @Transactional
  public void addToFavorites(Long productId) {
    Product product = getById(productId);
    product.setFavoritesCount(product.getFavoritesCount() + 1);
    productRepository.save(product);
  }

  /**
   * Decrement product favorites count Called when user removes product from favorites
   *
   * @param productId Product ID
   */
  @Transactional
  public void removeFromFavorites(Long productId) {
    Product product = getById(productId);
    if (product.getFavoritesCount() > 0) {
      product.setFavoritesCount(product.getFavoritesCount() - 1);
      productRepository.save(product);
    }
  }

  /**
   * Get product count by category Shows count badge on category filters
   *
   * @param categoryId Category ID
   * @return Number of products in category
   */
  public long getProductCountByCategory(Long categoryId) {
    return productRepository.countByCategoryId(categoryId);
  }
}
