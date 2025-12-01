package ecommerce_app.modules.cart.controller;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.cart.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart Controller", description = "Cart Management")
public class CartController {
  private final CartService cartService;

  @PostMapping("/add-to-cart")
  public ResponseEntity<BaseBodyResponse> addCart(@RequestParam Long productId) {
    return BaseBodyResponse.success(
        this.cartService.addNewProductToCart(productId), "Successfully added cart.");
  }

  @PostMapping("/items/{productId}/increment")
  public ResponseEntity<BaseBodyResponse> increment(
      @PathVariable(value = "productId", name = "productId") Long productId) {
    return BaseBodyResponse.success(
        this.cartService.incrementItem(productId), "Successfully increment cart.");
  }

  @PostMapping("/items/{productId}/decrement")
  public ResponseEntity<BaseBodyResponse> decrement(
      @PathVariable(name = "productId", value = "productId") Long productId) {
    return BaseBodyResponse.success(
        this.cartService.decrementItem(productId), "Successfully decrement cart.");
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse> getCart() {
    return BaseBodyResponse.success(this.cartService.getCart(), "Successfully get cart.");
  }
}
