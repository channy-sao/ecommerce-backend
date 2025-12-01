package ecommerce_app.modules.order.controller;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.order.model.dto.CheckoutRequest;
import ecommerce_app.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Management Order process")
public class OrderController {
  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<BaseBodyResponse> checkout(
      @RequestBody @Valid CheckoutRequest checkoutRequest) {
    return BaseBodyResponse.success(orderService.checkout(checkoutRequest), "Checkout successful");
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse> getOrders() {
    return BaseBodyResponse.success(orderService.getOrders(), "Orders successful");
  }
}
