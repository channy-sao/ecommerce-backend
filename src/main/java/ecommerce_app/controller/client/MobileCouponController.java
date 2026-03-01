// MobileCouponController.java
package ecommerce_app.controller.client;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.request.ApplyCouponRequest;
import ecommerce_app.dto.response.ApplyCouponResponse;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.service.CouponService;

import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Mobile Coupon Controller", description = "For user get promotion")
public class MobileCouponController {

  private final CouponService couponService;
  private final MessageSourceService messageSourceService;

  private String msg() {
    return messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS);
  }

  // Customer validates coupon before placing order
  @PostMapping("/apply")
  public ResponseEntity<BaseBodyResponse<ApplyCouponResponse>> apply(
      @Valid @RequestBody ApplyCouponRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {
    return BaseBodyResponse.success(couponService.applyCoupon(request, user.getId()), msg());
  }
}
