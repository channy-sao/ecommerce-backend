package ecommerce_app.api.client;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.product.service.MobileProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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

  /**
   * Get paginated product list for mobile app GET
   * /api/mobile/v1/products?page=0&pageSide=20&sort=createdAt,desc
   *
   * <p>Main product listing endpoint with filters
   */
  @GetMapping
  public ResponseEntity<BaseBodyResponse> getProducts(
      @RequestParam(value = "page", defaultValue = "0") int page,
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
    Pageable pageable = PageRequest.of(page, pageSide, Sort.by(direction, sort[0]));

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
  public ResponseEntity<BaseBodyResponse> getProductById(@PathVariable Long id) {
    return BaseBodyResponse.success(
        mobileProductService.getProductById(id), "Get product by ID successfully");
  }

  /**
   * Get single product detail by UUID GET /api/mobile/v1/products/uuid/{uuid}
   *
   * <p>Used for deep linking and product sharing
   */
  @GetMapping("/uuid/{uuid}")
  public ResponseEntity<BaseBodyResponse> getProductByUuid(@PathVariable UUID uuid) {

    return BaseBodyResponse.success(
        mobileProductService.getProductByUuid(uuid), "Get product by UUID successfully");
  }

  /**
   * Get featured products GET /api/mobile/v1/products/featured?size=10
   *
   * <p>Shows highlighted products on homepage
   */
  @GetMapping("/featured")
  public ResponseEntity<BaseBodyResponse> getFeaturedProducts(
      @RequestParam(defaultValue = "10") int size) {

    return BaseBodyResponse.success(
        mobileProductService.getFeaturedProducts(size), "Get featured products successfully");
  }

  /**
   * Get products on promotion GET /api/mobile/v1/products/promotions?page=0&size=20
   *
   * <p>Shows products currently on sale
   */
  @GetMapping("/promotions")
  public ResponseEntity<BaseBodyResponse> getPromotionalProducts(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

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
  public ResponseEntity<BaseBodyResponse> getProductsByCategory(
      @PathVariable Long categoryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

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
  public ResponseEntity<BaseBodyResponse> searchProducts(
      @RequestParam(value = "query") String query,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    return BaseBodyResponse.pageSuccess(
        mobileProductService.searchProducts(query, pageable), "Search products successfully");
  }

  /**
   * Get new arrivals GET /api/mobile/v1/products/new?size=10
   *
   * <p>Shows recently added products
   */
  @GetMapping("/new")
  public ResponseEntity<BaseBodyResponse> getNewArrivals(
      @RequestParam(defaultValue = "10") int size) {

    return BaseBodyResponse.success(
        mobileProductService.getNewArrivals(size), "Get new arrivals successfully");
  }

  /**
   * Get popular products GET /api/mobile/v1/products/popular?size=10
   *
   * <p>Shows most-liked products
   */
  @GetMapping("/popular")
  public ResponseEntity<BaseBodyResponse> getPopularProducts(
      @RequestParam(defaultValue = "10") int size) {

    return BaseBodyResponse.success(
        mobileProductService.getPopularProducts(size), "Get popular products successfully");
  }

  /**
   * Get related products GET /api/mobile/v1/products/{id}/related?size=5
   *
   * <p>Shows "You may also like" section on product detail page
   */
  @GetMapping("/{id}/related")
  public ResponseEntity<BaseBodyResponse> getRelatedProducts(
      @PathVariable Long id, @RequestParam(defaultValue = "5") int size) {

    return BaseBodyResponse.success(
        mobileProductService.getRelatedProducts(id, size), "Get related products successfully");
  }

  /**
   * Add product to favorites POST /api/mobile/v1/products/{id}/favorite
   *
   * <p>Increments favorites count when user likes a product
   */
  @PostMapping("/{id}/favorite")
  public ResponseEntity<BaseBodyResponse> addToFavorites(@PathVariable Long id) {
    mobileProductService.addToFavorites(id);
    return BaseBodyResponse.success(null, "Product added to favorites");
  }

  /**
   * Remove product from favorites DELETE /api/mobile/v1/products/{id}/favorite
   *
   * <p>Decrements favorites count when user unlikes a product
   */
  @DeleteMapping("/{id}/favorite")
  public ResponseEntity<BaseBodyResponse> removeFromFavorites(@PathVariable Long id) {
    mobileProductService.removeFromFavorites(id);
    return BaseBodyResponse.success(null, "Product removed from favorites");
  }
}
