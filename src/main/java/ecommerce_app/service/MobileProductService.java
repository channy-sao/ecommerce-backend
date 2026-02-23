package ecommerce_app.service;

import ecommerce_app.dto.response.MobileProductListResponse;
import ecommerce_app.dto.response.MobileProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Mobile Product Service Interface
 *
 * <p>Dedicated service for mobile app product operations Handles product browsing, searching, and
 * filtering for mobile clients
 */
public interface MobileProductService {

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
  Page<MobileProductListResponse> getProducts(
      Pageable pageable, Long categoryId, Boolean isFeature, Boolean hasPromotion, String search);

  /**
   * Get product by ID Used when user taps on a product card to see full details
   *
   * @param id Product ID
   * @param userId user ID for tracking views and personalized recommendations (optional)
   * @return Product entity with full details
   */
  MobileProductResponse getProductById(Long id, Long userId);

  /**
   * Get product by UUID Used for deep linking and sharing product URLs
   *
   * @param uuid Product UUID
   * @return Product entity
   */
  MobileProductResponse getProductByUuid(UUID uuid);

  /**
   * Get featured products Shows highlighted products on homepage
   *
   * @param limit Maximum number of products to return
   * @return List of featured products
   */
  List<MobileProductListResponse> getFeaturedProducts(int limit);

  /**
   * Get products with active promotions Shows products currently on sale
   *
   * @param pageable Pagination and sorting
   * @return Paginated list of products with active promotions
   */
  Page<MobileProductListResponse> getProductsOnPromotion(Pageable pageable);

  /**
   * Get products by category Used for category browsing screen
   *
   * @param categoryId Category ID
   * @param pageable Pagination and sorting
   * @return Paginated list of products in the category
   */
  Page<MobileProductListResponse> getProductsByCategory(Long categoryId, Pageable pageable);

  /**
   * Search products by query Main search functionality for mobile app
   *
   * @param query Search term
   * @param pageable Pagination and sorting
   * @return Paginated list of matching products
   */
  Page<MobileProductListResponse> searchProducts(String query, Pageable pageable);

  /**
   * Check if product is in stock Helper method for UI logic
   *
   * @param productId Product ID
   * @return true if product has stock available
   */
  boolean isProductInStock(Long productId);

  /**
   * Get product stock quantity Shows exact quantity to user if needed
   *
   * @param productId Product ID
   * @return Current stock quantity
   */
  int getProductStockQuantity(Long productId);

  /**
   * Check if product can be quick-added to cart Quick add is only available for simple products (no
   * active promotions)
   *
   * @param productId Product ID
   * @return true if product can be quick-added
   */
  boolean canQuickAddProduct(Long productId);

  /**
   * Get related products (same category) Shows "You may also like" section
   *
   * @param productId Current product ID
   * @param page is page indes=x
   * @param size Maximum number of related products
   * @return List of products in same category
   */
  Page<MobileProductListResponse> getRelatedProducts(Long productId, int page, int size);

  /**
   * Get new arrivals Shows recently added products
   *
   * @param limit Maximum number of products
   * @return List of newest products
   */
  List<MobileProductListResponse> getNewArrivals(int limit);

  /**
   * Get popular products (by favorites count) Shows most-liked products
   *
   * @param page is page index
   * @param size Maximum number of products
   * @return Page of most popular products
   */
  Page<MobileProductListResponse> getPopularProducts(int page, int size);

  /**
   * Get low stock products Shows "Only X left!" products
   *
   * @param limit Maximum number of products
   * @return List of low stock products
   */
  List<MobileProductListResponse> getLowStockProducts(int limit);

  /**
   * Increment product favorites count Called when user adds product to favorites
   *
   * @param productId Product ID
   */
  void addToFavorites(Long productId);

  /**
   * Decrement product favorites count Called when user removes product from favorites
   *
   * @param productId Product ID
   */
  void removeFromFavorites(Long productId);

  /**
   * Get product count by category Shows count badge on category filters
   *
   * @param categoryId Category ID
   * @return Number of products in category
   */
  long getProductCountByCategory(Long categoryId);

  /**
   * Get personalized product recommendations for the authenticated user.
   *
   * <p>Returns products based on user's view history. Falls back to popular products if user has no
   * history yet.
   *
   * @param userId authenticated user ID (never null)
   * @param excludeIds product IDs to exclude (already seen), can be null
   * @param page page number for pagination
   * @param size number of products per page
   * @return paginated list of recommended or popular products
   */
  Page<MobileProductListResponse> getRecommendedProducts(
      Long userId, List<Long> excludeIds, int page, int size);

  /**
   * Track a product view for recommendation history. Skips if userId is null (guest) or already
   * viewed.
   *
   * @param userId authenticated user ID, null means guest
   * @param productId product being viewed
   */
  void trackProductView(Long userId, Long productId);
}
