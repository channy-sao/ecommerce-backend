package ecommerce_app.controller.client;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.MobileProductListResponse;
import ecommerce_app.dto.response.MobileProductResponse;
import ecommerce_app.service.MobileProductService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Mobile Product Controller
 *
 * <p>Handles all product-related endpoints for the mobile app Uses dedicated MobileProductService
 * for mobile-specific logic
 */
@RestController
@RequestMapping("/api/client/v1/products")
@RequiredArgsConstructor
@Tag(name = "Mobile Product Controller", description = "Product APIs for mobile app")
public class MobileProductController {

  private final MobileProductService mobileProductService;
  private final MessageSourceService messageSourceService;

  /**
   * Get paginated product list for mobile app GET
   * /api/mobile/v1/products?page=0&pageSide=20&sort=createdAt,desc
   *
   * <p>Main product listing endpoint with filters
   */
  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<MobileProductListResponse>>> getProducts(
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSide,
      @RequestParam(value = "categoryId", required = false) Long categoryId,
      @RequestParam(value = "isFeature", required = false) Boolean isFeature,
      @RequestParam(required = false) Boolean hasPromotion,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

    // Create pageable with sorting
    Sort.Direction direction =
        sort.length > 1 && sort[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page - 1, pageSide, Sort.by(direction, sort[0]));

    return BaseBodyResponse.pageSuccess(
        mobileProductService.getProducts(pageable, categoryId, isFeature, hasPromotion, search),
        "Get products successfully");
  }

  /**
   * Get single product detail by ID GET /api/mobile/v1/products/{id}
   *
   * <p>Used when user taps on a product card
   */
  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<MobileProductResponse>> getProductById(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
    Long userId = user != null ? user.getId() : null;
    return BaseBodyResponse.success(
        mobileProductService.getProductById(id, userId), "Get product by ID successfully");
  }

  /**
   * Get single product detail by UUID GET /api/mobile/v1/products/uuid/{uuid}
   *
   * <p>Used for deep linking and product sharing
   */
  @GetMapping("/uuid/{uuid}")
  public ResponseEntity<BaseBodyResponse<MobileProductResponse>> getProductByUuid(
      @PathVariable UUID uuid) {

    return BaseBodyResponse.success(
        mobileProductService.getProductByUuid(uuid), "Get product by UUID successfully");
  }

  /**
   * Get featured products GET /api/mobile/v1/products/featured?size=10
   *
   * <p>Shows highlighted products on homepage
   */
  @GetMapping("/featured")
  public ResponseEntity<BaseBodyResponse<List<MobileProductListResponse>>> getFeaturedProducts(
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

    return BaseBodyResponse.pageSuccess(
        mobileProductService.getFeaturedProducts(page, pageSize),
        "Get featured products successfully");
  }

  /**
   * Get products on promotion GET /api/mobile/v1/products/promotions?page=0&size=20
   *
   * <p>Shows products currently on sale
   */
  @GetMapping("/promotions")
  public ResponseEntity<BaseBodyResponse<List<MobileProductListResponse>>> getPromotionalProducts(
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

    Pageable pageable =
        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    return BaseBodyResponse.pageSuccess(
        mobileProductService.getProductsOnPromotion(pageable),
        "Get promotional products successfully");
  }

  /**
   * Get products by category GET /api/mobile/v1/products/category/{categoryId}?page=0&size=20
   *
   * <p>Used for category browsing screen
   */
  @GetMapping("/category/{categoryId}")
  public ResponseEntity<BaseBodyResponse<List<MobileProductListResponse>>> getProductsByCategory(
      @PathVariable Long categoryId,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

    Pageable pageable =
        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    return BaseBodyResponse.pageSuccess(
        mobileProductService.getProductsByCategory(categoryId, pageable),
        "Get products by category successfully");
  }

  /**
   * Search products GET /api/mobile/v1/products/search?q=laptop&page=0&pageSize=20
   *
   * <p>Main search functionality
   */
  @GetMapping("/search")
  public ResponseEntity<BaseBodyResponse<List<MobileProductListResponse>>> searchProducts(
      @RequestParam(value = "query") String query,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

    Pageable pageable =
        PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    return BaseBodyResponse.pageSuccess(
        mobileProductService.searchProducts(query, pageable), "Search products successfully");
  }

  /**
   * Get new arrivals GET /api/mobile/v1/products/new?size=10
   *
   * <p>Shows recently added products
   */
  @GetMapping("/new")
  public ResponseEntity<BaseBodyResponse<List<MobileProductListResponse>>> getNewArrivals(
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

    return BaseBodyResponse.pageSuccess(
        mobileProductService.getNewArrivals(page, pageSize), "Get new arrivals successfully");
  }

  /**
   * Get popular products GET /api/mobile/v1/products/popular?size=10
   *
   * <p>Shows most-liked products
   */
  @GetMapping("/popular")
  public ResponseEntity<BaseBodyResponse<List<MobileProductListResponse>>> getPopularProducts(
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

    return BaseBodyResponse.pageSuccess(
        mobileProductService.getPopularProducts(page, pageSize),
        "Get popular products successfully");
  }

  /**
   * GET /api/mobile/products/{id}/related?size=5 Returns related products for "You may also like"
   * section
   */
  @GetMapping("/{id}/related")
  public ResponseEntity<BaseBodyResponse<List<MobileProductListResponse>>> getRelatedProducts(
      @PathVariable Long id,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

    return BaseBodyResponse.pageSuccess(
        mobileProductService.getRelatedProducts(id, page, pageSize),
        "Get related products successfully");
  }

  /**
   * Add product to favorites POST /api/mobile/v1/products/{id}/favorite
   *
   * <p>Increments favorites count when user likes a product
   */
  @PostMapping("/{id}/favorite")
  public ResponseEntity<BaseBodyResponse<Void>> addToFavorites(@PathVariable Long id) {
    mobileProductService.addToFavorites(id);
    return BaseBodyResponse.success("Product added to favorites");
  }

  /**
   * Remove product from favorites DELETE /api/mobile/v1/products/{id}/favorite
   *
   * <p>Decrements favorites count when user unlikes a product
   */
  @DeleteMapping("/{id}/favorite")
  public ResponseEntity<BaseBodyResponse<Void>> removeFromFavorites(@PathVariable Long id) {
    mobileProductService.removeFromFavorites(id);
    return BaseBodyResponse.success("Product removed from favorites");
  }

  /**
   * Get personalized product recommendations for the authenticated user.
   *
   * <p>Returns products based on the user's view history (categories they browsed most in the last
   * 30 days). If the user has no view history, falls back to popular products.
   *
   * <p>This endpoint requires authentication. Mobile should:
   *
   * <ul>
   *   <li>Only call this when user is logged in
   *   <li>Use {@code GET /products/popular} instead for guest users
   *   <li>Pass previously seen product IDs via {@code excludeIds} to avoid duplicates
   * </ul>
   *
   * @param userDetails authenticated user from JWT token (never null — endpoint is secured)
   * @param page page number for pagination, default 0
   * @param size number of products per page, default 10
   * @param excludeIds list of product IDs to exclude (already viewed/seen by user)
   * @return paginated list of recommended products personalized for the user
   */
  @GetMapping("/recommended")
  public ResponseEntity<BaseBodyResponse<List<MobileProductListResponse>>> getRecommended(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) List<Long> excludeIds) {

    return BaseBodyResponse.pageSuccess(
        mobileProductService.getRecommendedProducts(userDetails.getId(), excludeIds, page, size),
        "Get recommended products successfully");
  }

  /**
   * Retrieves a list of product name suggestions based on the given search query.
   *
   * <p>This endpoint is intended for use in search autocomplete/typeahead UI components. Returns up
   * to 8 matching product names that contain the query string (case-insensitive). Query must be at
   * least 2 characters to return results.
   *
   * @param q the search query string (minimum 2 characters recommended)
   * @return a {@link ResponseEntity} containing a list of matching product name suggestions
   */
  @GetMapping("/suggestions")
  public ResponseEntity<BaseBodyResponse<List<String>>> getSuggestions(@RequestParam String q) {
    return BaseBodyResponse.success(
        mobileProductService.getSuggestions(q),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
