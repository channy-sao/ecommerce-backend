package ecommerce_app.controller.client;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.request.CheckoutRequest;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;
import ecommerce_app.service.OrderService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Client Order process")
public class OrderController {
  private final OrderService orderService;
  private final MessageSourceService messageSourceService;

  @PostMapping("/checkout")
  public ResponseEntity<BaseBodyResponse<OrderResponse>> checkout(
      @RequestBody @Valid CheckoutRequest checkoutRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        orderService.checkout(checkoutRequest, userDetails.getId()),
        messageSourceService.getMessage(MessageKeyConstant.ORDER_MESSAGE_PLACE_SUCCESS));
  }

  @PatchMapping("/{orderId}/cancel")
  public ResponseEntity<BaseBodyResponse<Void>> cancelOrder(
      @PathVariable Long orderId,
      @RequestParam(required = false) String reason,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    this.orderService.cancelOrder(orderId, userDetails.getId(), reason);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.ORDER_MESSAGE_PLACE_SUCCESS));
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<OrderResponse>>> getOrders(
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.pageSuccess(
        orderService.getOrders(userDetails.getId(), page, pageSize),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<BaseBodyResponse<OrderDetailResponse>> getOrderDetail(
      @PathVariable(value = "orderId", name = "orderId") Long orderId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseBodyResponse.success(
        orderService.getOrderDetails(orderId, userDetails.getId()),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
