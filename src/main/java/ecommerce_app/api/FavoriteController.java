package ecommerce_app.api;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.product.service.FavoriteService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
  private final MessageSourceService messageSourceService;

  @PostMapping("/products/{productId}")
  public ResponseEntity<BaseBodyResponse<Void>> favorite(
      @PathVariable(value = "productId") Long productId,
      @AuthenticationPrincipal CustomUserDetails user) {
    favoriteService.favorite(user.getId(), productId);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @DeleteMapping("/products/{productId}")
  public ResponseEntity<BaseBodyResponse<Void>> unfavorite(
      @PathVariable(value = "productId") Long productId,
      @AuthenticationPrincipal CustomUserDetails user) {
    favoriteService.unfavorite(user.getId(), productId);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/products/{productId}/toggle")
  public ResponseEntity<BaseBodyResponse<Void>> toggle(
      @PathVariable(value = "productId") Long productId,
      @AuthenticationPrincipal CustomUserDetails user) {
    favoriteService.toggleFavorite(user.getId(), productId);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @GetMapping("/products")
  public ResponseEntity<BaseBodyResponse<List<Long>>> getFavoriteProducts(
      @AuthenticationPrincipal CustomUserDetails user) {
    return BaseBodyResponse.success(
        favoriteService.getFavoriteProductIds(user.getId()),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @GetMapping("/products/{productId}/count")
  public ResponseEntity<BaseBodyResponse<Long>> countFavorites(
      @PathVariable(value = "productId") Long productId) {
    return BaseBodyResponse.success(
        favoriteService.countFavorites(productId),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @GetMapping("/products/{productId}/is-favorite")
  public ResponseEntity<BaseBodyResponse<Boolean>> isFavorite(
      @PathVariable(value = "productId") Long productId,
      @AuthenticationPrincipal CustomUserDetails user) {
    return BaseBodyResponse.success(
        favoriteService.isFavorite(user.getId(), productId),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
