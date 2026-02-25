// CouponService.java
package ecommerce_app.service;

import ecommerce_app.dto.request.ApplyCouponRequest;
import ecommerce_app.dto.request.CouponRequest;
import ecommerce_app.dto.response.ApplyCouponResponse;
import ecommerce_app.dto.response.CouponResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CouponService {
  Page<CouponResponse> getAllCoupons(Pageable pageable);

  CouponResponse getCouponById(Long id);

  CouponResponse createCoupon(CouponRequest request);

  CouponResponse updateCoupon(Long id, CouponRequest request);

  CouponResponse toggleStatus(Long id);

  void deleteCoupon(Long id);

  // Mobile
  ApplyCouponResponse applyCoupon(ApplyCouponRequest request, Long userId);

  void redeemCoupon(Long couponId, Long userId, Long orderId, BigDecimal discountAmount);
}
