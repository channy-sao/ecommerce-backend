package ecommerce_app.api.client;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.cart.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/v1/carts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart Controller", description = "Cart Management")
public class CartController {
  private final CartService cartService;

  @PostMapping("/add-to-cart")
  public ResponseEntity<BaseBodyResponse> addCart(
      @RequestParam Long productId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        this.cartService.addNewProductToCart(productId, userDetails.getId()),
        "Successfully added cart.");
  }

  @PostMapping("/items/{itemId}/increment")
  public ResponseEntity<BaseBodyResponse> increment(
      @PathVariable(value = "itemId", name = "itemId") Long itemId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        this.cartService.incrementItem(itemId, userDetails.getId()),
        "Successfully increment cart.");
  }

  @PostMapping("/items/{itemId}/decrement")
  public ResponseEntity<BaseBodyResponse> decrement(
      @PathVariable(name = "itemId", value = "itemId") Long itemId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        this.cartService.decrementItem(itemId, userDetails.getId()),
        "Successfully decrement cart.");
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse> getCart(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        this.cartService.getCart(userDetails.getId()), "Successfully get cart.");
  }
}
