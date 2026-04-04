package ecommerce_app.controller.admin;

import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/staff/payments")
@RequiredArgsConstructor
@Tag(name = "Staff Payment Controller", description = "Staff payment management")
public class StaffPaymentController {

  private final PaymentService paymentService;

  /**
   * Staff marks COD payment as collected when cash is received at delivery.
   *
   * <p>PATCH /api/staff/v1/payments/orders/{orderId}/cod/mark-paid
   */
  @PatchMapping("/orders/{orderId}/cod/mark-paid")
  @Operation(summary = "Mark COD payment as collected")
  public ResponseEntity<BaseBodyResponse<Void>> markCodPaid(
      @PathVariable Long orderId, @AuthenticationPrincipal CustomUserDetails userDetails) {

    paymentService.markCodPaid(orderId, userDetails.getId());
    return BaseBodyResponse.success("COD payment marked as collected");
  }

  /**
   * Staff marks Cash-in-Shop payment as collected when customer pays at store.
   *
   * <p>PATCH /api/staff/v1/payments/orders/{orderId}/cash-in-shop/mark-paid
   */
  @PatchMapping("/orders/{orderId}/cash-in-shop/mark-paid")
  @Operation(summary = "Mark Cash-in-Shop payment as collected")
  public ResponseEntity<BaseBodyResponse<Void>> markCashInShopPaid(
      @PathVariable Long orderId, @AuthenticationPrincipal CustomUserDetails userDetails) {

    paymentService.markCashInShopPaid(orderId, userDetails.getId());
    return BaseBodyResponse.success("Cash-in-Shop payment marked as collected");
  }
}
