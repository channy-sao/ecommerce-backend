// AdminCouponController.java
package ecommerce_app.controller.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.request.CouponRequest;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.CouponResponse;
import ecommerce_app.service.CouponService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Admin Coupon Controller", description = "For admin manage coupons")
public class AdminCouponController {

  private final CouponService couponService;
  private final MessageSourceService messageSourceService;

  private String msg() {
    return messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS);
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<CouponResponse>>> getAll(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
    return BaseBodyResponse.pageSuccess(
        couponService.getAllCoupons(
            PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))),
        msg());
  }

  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<CouponResponse>> getById(@PathVariable Long id) {
    return BaseBodyResponse.success(couponService.getCouponById(id), msg());
  }

  @PostMapping
  public ResponseEntity<BaseBodyResponse<CouponResponse>> create(@Valid @RequestBody CouponRequest request) {
    return BaseBodyResponse.success(couponService.createCoupon(request), msg());
  }

  @PutMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<CouponResponse>> update(
      @PathVariable Long id, @Valid @RequestBody CouponRequest request) {
    return BaseBodyResponse.success(couponService.updateCoupon(id, request), msg());
  }

  @PatchMapping("/{id}/toggle-status")
  public ResponseEntity<BaseBodyResponse<CouponResponse>> toggleStatus(@PathVariable Long id) {
    return BaseBodyResponse.success(couponService.toggleStatus(id), msg());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<Void>> delete(@PathVariable Long id) {
    couponService.deleteCoupon(id);
    return BaseBodyResponse.success(msg());
  }
}
