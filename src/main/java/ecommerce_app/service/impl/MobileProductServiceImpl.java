package ecommerce_app.service.impl;

import ecommerce_app.dto.response.UserResponse;
import ecommerce_app.entity.ProductView;
import ecommerce_app.entity.User;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.ProductMapper;
import ecommerce_app.dto.response.MobileProductListResponse;
import ecommerce_app.dto.response.MobileProductResponse;
import ecommerce_app.entity.Product;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.repository.ProductViewRepository;
import ecommerce_app.repository.UserRepository;
import ecommerce_app.service.MobileProductService;
import ecommerce_app.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
  private final ProductViewRepository productViewRepository;
  private final UserRepository userRepository;

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
  public MobileProductResponse getProductById(Long id, Long userId) {
    final var product = getById(id);
    // Track asynchronously — don't slow down the response
    if (userId != null) {
      CompletableFuture.runAsync(() -> trackProductView(userId, id));
    }
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
   * @param page is page index
   * @param pageSize Maximum number of products to return
   * @return List of featured products
   */
  public Page<MobileProductListResponse> getFeaturedProducts(int page, int pageSize) {
    Pageable pageable = PageRequest.of(page-1, pageSize);
    return productRepository.findByIsFeatureTrue(pageable).map(productMapper::toListResponse);
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
   * Get related products by same category, exclude current product, in stock only. Used for "You
   * may also like" section on product detail screen.
   */
  public Page<MobileProductListResponse> getRelatedProducts(
      Long productId, int page, int pageSize) {
    Product product = getById(productId);

    if (product.getCategory() == null) {
      return Page.empty();
    }

    return productRepository
        .findRelatedProducts(
            product.getCategory().getId(),
            productId,
            PageRequest.of(
                page - 1,
                pageSize)) // page -1 because client sends 1-based page index, but Spring Data uses
        // 0-based
        .map(productMapper::toListResponse);
  }

  /**
   * Get new arrivals Shows recently added products
   *
   * @param page is page index
   * @param pageSize Maximum number of products
   * @return List of newest products
   */
  public Page<MobileProductListResponse> getNewArrivals(int page, int pageSize) {
    Pageable pageable =
        PageRequest.of(
            page-1,
            pageSize,
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    return productRepository.findAll(pageable).map(productMapper::toListResponse);
  }

  /**
   * Get popular products (by favorites count) Shows most-liked products
   *
   * @param page is page index
   * @param size Maximum number of products
   * @return Page of most popular products
   */
  public Page<MobileProductListResponse> getPopularProducts(int page, int size) {
    return productRepository
        .findPopularProducts(PageRequest.of(page, size))
        .map(productMapper::toListResponse);
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
    Pageable pageable = PageRequest.of(0, limit);

    return productRepository
        .findLowStockProducts(10, pageable) // 10 = low stock threshold
        .map(productMapper::toListResponse)
        .getContent();
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

  /**
   * Track a product view for the authenticated user.
   *
   * <p>Used to build user's view history for personalized recommendations.
   * Duplicate views (same user + same product) are ignored to keep history clean.
   *
   * <p>This method is called asynchronously from {@link #getProductById} so it
   * does not affect response time of the product detail endpoint.
   *
   * <p>Example:
   * <pre>
   *   User opens Nike Shoes → saved to product_views
   *   User opens Nike Shoes again → ignored (duplicate)
   *   User opens Adidas Shoes → saved to product_views
   * </pre>
   *
   * @param userId    authenticated user ID, null means guest (skips tracking)
   * @param productId product being viewed
   * @throws ResourceNotFoundException if product or user not found
   */
  public void trackProductView(Long userId, Long productId) {
    // Guest user — skip tracking
    if (userId == null) return;

    // Don't save duplicate views
    if (productViewRepository.existsByUserIdAndProductId(userId, productId)) {
      return;
    }

    Product product = getById(productId);
    User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User " + userId));

    productViewRepository.save(
            ProductView.builder()
                    .user(user)
                    .product(product)
                    .viewedAt(LocalDateTime.now())
                    .build());
  }

  @Override
  public List<String> getSuggestions(String q) {
    if (q == null || q.trim().length() < 2) return List.of();
    return productRepository.findSuggestions(q.trim());
  }

  /**
   * Get personalized product recommendations for the authenticated user.
   *
   * <p>Strategy:
   *
   * <ol>
   *   <li>Find top 3 categories the user viewed most in the last 30 days
   *   <li>If history exists → recommend products from those categories
   *   <li>If no history → fallback to popular products (new user)
   * </ol>
   *
   * <p>Example:
   *
   * <pre>
   *   User viewed: Nike Shoes, Adidas Shoes, Puma Shoes
   *          ↓
   *   Top category: Shoes
   *          ↓
   *   Returns: more products from Shoes category
   * </pre>
   *
   * @param userId authenticated user ID (never null — guaranteed by controller)
   * @param excludeIds product IDs to exclude (already seen by user), can be null
   * @param page page number for pagination
   * @param size number of products per page
   * @return paginated list of recommended products, or popular products if no history
   */
  public Page<MobileProductListResponse> getRecommendedProducts(
      Long userId, List<Long> excludeIds, int page, int size) {

    List<Long> allExcluded = excludeIds != null ? new ArrayList<>(excludeIds) : new ArrayList<>();

    // Find top categories user viewed in last 30 days
    LocalDateTime since = LocalDateTime.now().minusDays(30);
    List<Long> topCategories =
        productViewRepository.findTopCategoryIdsByUserId(
            userId, since, PageRequest.of(0, 3)); // top 3 categories

    // Has history → recommend from their interests
    if (!topCategories.isEmpty()) {
      return productRepository
          .findRecommendedProducts(topCategories, allExcluded, PageRequest.of(page, size))
          .map(productMapper::toListResponse);
    }

    // No history → fallback to popular products
    return getPopularProducts(page, size);
  }
}
