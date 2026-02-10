package ecommerce_app.api.client;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.product.model.dto.MobileProductListResponse;
import ecommerce_app.modules.product.model.entity.Product;

import ecommerce_app.modules.product.service.ProductService;
import ecommerce_app.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client/v1/products")
@RequiredArgsConstructor
public class MobileProductController {

  private final ProductService productService;
  private final ProductMapper productMapper;

  /**
   * Get paginated product list for mobile app GET
   * /api/mobile/v1/products?page=0&size=20&sort=createdAt,desc
   */
  @GetMapping
  public ResponseEntity<BaseBodyResponse> getProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Boolean isFeature,
      @RequestParam(required = false) Boolean hasPromotion,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

    // Create pageable with sorting
    Sort.Direction direction =
        sort.length > 1 && sort[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

    // Fetch products (implement filtering in service)
    Page<Product> productPage =
        productService.findAll(pageable, categoryId, isFeature, hasPromotion, search);

    // Map to DTOs
    List<MobileProductListResponse> content =
        productPage.getContent().stream()
            .map(productMapper::toListResponse)
            .collect(Collectors.toList());

    // Create paginated response
    PageResponse<MobileProductListResponse> pageResponse =
        PageResponse.of(
            content,
            productPage.getNumber(),
            productPage.getSize(),
            productPage.getTotalElements());

    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  /** Get single product detail by ID GET /api/mobile/v1/products/{id} */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<MobileProductResponse>> getProductById(@PathVariable Long id) {
    Product product = productService.findById(id);
    MobileProductResponse response = productMapper.toDetailResponse(product);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /** Get single product detail by UUID GET /api/mobile/v1/products/uuid/{uuid} */
  @GetMapping("/uuid/{uuid}")
  public ResponseEntity<ApiResponse<MobileProductResponse>> getProductByUuid(
      @PathVariable UUID uuid) {
    Product product = productService.findByUuid(uuid);
    MobileProductResponse response = productMapper.toDetailResponse(product);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /** Get featured products GET /api/mobile/v1/products/featured?size=10 */
  @GetMapping("/featured")
  public ResponseEntity<ApiResponse<List<MobileProductListResponse>>> getFeaturedProducts(
      @RequestParam(defaultValue = "10") int size) {

    List<Product> products = productService.findFeaturedProducts(size);
    List<MobileProductListResponse> response =
        products.stream().map(productMapper::toListResponse).collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /** Get products on promotion GET /api/mobile/v1/products/promotions?page=0&size=20 */
  @GetMapping("/promotions")
  public ResponseEntity<ApiResponse<PageResponse<MobileProductListResponse>>>
      getPromotionalProducts(
          @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Product> productPage = productService.findProductsWithActivePromotions(pageable);

    List<MobileProductListResponse> content =
        productPage.getContent().stream()
            .map(productMapper::toListResponse)
            .collect(Collectors.toList());

    PageResponse<MobileProductListResponse> pageResponse =
        PageResponse.of(
            content,
            productPage.getNumber(),
            productPage.getSize(),
            productPage.getTotalElements());

    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  /** Get products by category GET /api/mobile/v1/products/category/{categoryId}?page=0&size=20 */
  @GetMapping("/category/{categoryId}")
  public ResponseEntity<ApiResponse<PageResponse<MobileProductListResponse>>> getProductsByCategory(
      @PathVariable Long categoryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Product> productPage = productService.findByCategory(categoryId, pageable);

    List<MobileProductListResponse> content =
        productPage.getContent().stream()
            .map(productMapper::toListResponse)
            .collect(Collectors.toList());

    PageResponse<MobileProductListResponse> pageResponse =
        PageResponse.of(
            content,
            productPage.getNumber(),
            productPage.getSize(),
            productPage.getTotalElements());

    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  /** Search products GET /api/mobile/v1/products/search?q=laptop&page=0&size=20 */
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<PageResponse<MobileProductListResponse>>> searchProducts(
      @RequestParam String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Product> productPage = productService.searchProducts(q, pageable);

    List<MobileProductListResponse> content =
        productPage.getContent().stream()
            .map(productMapper::toListResponse)
            .collect(Collectors.toList());

    PageResponse<MobileProductListResponse> pageResponse =
        PageResponse.of(
            content,
            productPage.getNumber(),
            productPage.getSize(),
            productPage.getTotalElements());

    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }
}
