package ecommerce_app.api.client;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.order.model.dto.CheckoutRequest;
import ecommerce_app.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Client Order process")
public class OrderController {
  private final OrderService orderService;

  @PostMapping("/checkout")
  public ResponseEntity<BaseBodyResponse> checkout(
      @RequestBody @Valid CheckoutRequest checkoutRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        orderService.checkout(checkoutRequest, userDetails.getId()), "Checkout successful");
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse> getOrders(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        orderService.getOrders(userDetails.getId()), "Orders successful");
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<BaseBodyResponse> getOrderDetail(
      @PathVariable(value = "orderId", name = "orderId") Long orderId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        orderService.getOrderDetails(orderId, userDetails.getId()), "Get Order detail successful");
  }
}
