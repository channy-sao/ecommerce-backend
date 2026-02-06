package ecommerce_app.api;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.product.service.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "Favorite Controller", description = "For Management User Favorite Products")
public class FavoriteController {
  private final FavoriteService favoriteService;

  @PostMapping("/products/{productId}")
  public ResponseEntity<BaseBodyResponse> favorite(
      @PathVariable(value = "productId") Long productId,
      @AuthenticationPrincipal CustomUserDetails user) {
    favoriteService.favorite(user.getId(), productId);
    return BaseBodyResponse.success(null, "Product added to favorites");
  }

  @DeleteMapping("/products/{productId}")
  public ResponseEntity<BaseBodyResponse> unfavorite(
      @PathVariable(value = "productId") Long productId,
      @AuthenticationPrincipal CustomUserDetails user) {
    favoriteService.unfavorite(user.getId(), productId);
    return BaseBodyResponse.success(null, "Product removed from favorites");
  }

  @PostMapping("/products/{productId}/toggle")
  public ResponseEntity<BaseBodyResponse> toggle(
      @PathVariable(value = "productId") Long productId,
      @AuthenticationPrincipal CustomUserDetails user) {
    favoriteService.toggleFavorite(user.getId(), productId);
    return BaseBodyResponse.success(null, "Product favorite status toggled");
  }

  @GetMapping("/products")
  public ResponseEntity<BaseBodyResponse> getFavoriteProducts(
      @AuthenticationPrincipal CustomUserDetails user) {
    return BaseBodyResponse.success(
        favoriteService.getFavoriteProductIds(user.getId()),
        "Favorite products retrieved successfully");
  }

  @GetMapping("/products/{productId}/count")
  public ResponseEntity<BaseBodyResponse> countFavorites(
      @PathVariable(value = "productId") Long productId) {
    return BaseBodyResponse.success(
        favoriteService.countFavorites(productId), "Favorite count retrieved successfully");
  }

  @GetMapping("/products/{productId}/is-favorite")
  public ResponseEntity<BaseBodyResponse> isFavorite(
      @PathVariable(value = "productId") Long productId,
      @AuthenticationPrincipal CustomUserDetails user) {
    return BaseBodyResponse.success(
        favoriteService.isFavorite(user.getId(), productId),
        "Favorite status retrieved successfully");
  }
}
