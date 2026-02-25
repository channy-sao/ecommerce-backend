// CouponServiceImpl.java
package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.CouponDiscountType;
import ecommerce_app.dto.request.ApplyCouponRequest;
import ecommerce_app.dto.request.CouponRequest;
import ecommerce_app.dto.response.ApplyCouponResponse;
import ecommerce_app.dto.response.CouponResponse;
import ecommerce_app.entity.Coupon;
import ecommerce_app.entity.CouponUsage;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ConflictException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.CouponMapper;
import ecommerce_app.repository.CouponRepository;
import ecommerce_app.repository.CouponUsageRepository;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.repository.UserRepository;
import ecommerce_app.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

  private final CouponRepository couponRepository;
  private final CouponUsageRepository couponUsageRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final CouponMapper couponMapper;

  // ── Admin ─────────────────────────────────────────────────────────────────

  @Override
  public Page<CouponResponse> getAllCoupons(Pageable pageable) {
    return couponRepository.findAll(pageable).map(couponMapper::toResponse);
  }

  @Override
  public CouponResponse getCouponById(Long id) {
    return couponMapper.toResponse(findById(id));
  }

  @Override
  @Transactional
  public CouponResponse createCoupon(CouponRequest request) {
    if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
      throw new ConflictException("Coupon code already exists: " + request.getCode());
    }
    validateRequest(request);

    Coupon coupon =
        Coupon.builder()
            .code(request.getCode().toUpperCase().trim())
            .description(request.getDescription())
            .discountType(request.getDiscountType())
            .discountValue(request.getDiscountValue())
            .minOrderAmount(request.getMinOrderAmount())
            .maxDiscount(request.getMaxDiscount())
            .usageLimit(request.getUsageLimit())
            .usagePerUser(request.getUsagePerUser() != null ? request.getUsagePerUser() : 1)
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();

    return couponMapper.toResponse(couponRepository.save(coupon));
  }

  @Override
  @Transactional
  public CouponResponse updateCoupon(Long id, CouponRequest request) {
    Coupon coupon = findById(id);
    validateRequest(request);

    // Check code conflict (exclude self)
    couponRepository
        .findByCodeIgnoreCase(request.getCode())
        .filter(c -> !c.getId().equals(id))
        .ifPresent(
            c -> {
              throw new ConflictException("Coupon code already exists");
            });

    coupon.setCode(request.getCode().toUpperCase().trim());
    coupon.setDescription(request.getDescription());
    coupon.setDiscountType(request.getDiscountType());
    coupon.setDiscountValue(request.getDiscountValue());
    coupon.setMinOrderAmount(request.getMinOrderAmount());
    coupon.setMaxDiscount(request.getMaxDiscount());
    coupon.setUsageLimit(request.getUsageLimit());
    coupon.setUsagePerUser(request.getUsagePerUser() != null ? request.getUsagePerUser() : 1);
    coupon.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
    coupon.setStartDate(request.getStartDate());
    coupon.setEndDate(request.getEndDate());

    return couponMapper.toResponse(couponRepository.save(coupon));
  }

  @Override
  @Transactional
  public CouponResponse toggleStatus(Long id) {
    Coupon coupon = findById(id);
    coupon.setIsActive(!coupon.getIsActive());
    return couponMapper.toResponse(couponRepository.save(coupon));
  }

  @Override
  @Transactional
  public void deleteCoupon(Long id) {
    couponRepository.delete(findById(id));
  }

  // ── Mobile ────────────────────────────────────────────────────────────────

  @Override
  public ApplyCouponResponse applyCoupon(ApplyCouponRequest request, Long userId) {
    // 1 — Find valid coupon
    Coupon coupon =
        couponRepository
            .findValidByCode(request.getCode(), LocalDateTime.now())
            .orElseThrow(() -> new BadRequestException("Coupon is invalid or expired"));

    // 2 — Check total usage limit
    if (coupon.hasReachedUsageLimit()) {
      throw new BadRequestException("Coupon has reached its usage limit");
    }

    // 3 — Check per-user usage limit
    int userUsageCount = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
    if (userUsageCount >= coupon.getUsagePerUser()) {
      throw new BadRequestException("You have already used this coupon");
    }

    // 4 — Check minimum order amount
    if (coupon.getMinOrderAmount() != null
        && request.getOrderTotal().compareTo(coupon.getMinOrderAmount()) < 0) {
      throw new BadRequestException(
          String.format(
              "Minimum order amount is $%.2f to use this coupon", coupon.getMinOrderAmount()));
    }

    // 5 — Calculate discount amount
    BigDecimal discountAmount = calculateDiscount(coupon, request.getOrderTotal());
    BigDecimal finalTotal =
        request.getOrderTotal().subtract(discountAmount).max(BigDecimal.ZERO); // never go below 0

    String message = String.format("Coupon applied! You save $%.2f", discountAmount);

    return ApplyCouponResponse.builder()
        .couponId(coupon.getId())
        .code(coupon.getCode())
        .discountType(coupon.getDiscountType())
        .discountValue(coupon.getDiscountValue())
        .discountAmount(discountAmount)
        .finalTotal(finalTotal)
        .message(message)
        .build();
  }

  @Override
  @Transactional
  public void redeemCoupon(Long couponId, Long userId, Long orderId, BigDecimal discountAmount) {
    Coupon coupon = findById(couponId);

    // Record usage
    CouponUsage usage =
        CouponUsage.builder()
            .coupon(coupon)
            .user(userRepository.getReferenceById(userId))
            .order(orderRepository.getReferenceById(orderId))
            .discountAmount(discountAmount)
            .build();
    couponUsageRepository.save(usage);

    // Increment used count
    coupon.setUsedCount(coupon.getUsedCount() + 1);
    couponRepository.save(coupon);
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private Coupon findById(Long id) {
    return couponRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
  }

  private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderTotal) {
    return switch (coupon.getDiscountType()) {
      case PERCENTAGE -> {
        BigDecimal discount =
            orderTotal
                .multiply(coupon.getDiscountValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        // Apply max discount cap if set
        if (coupon.getMaxDiscount() != null) {
          discount = discount.min(coupon.getMaxDiscount());
        }
        yield discount;
      }
      case FIXED_AMOUNT -> {
        // Can't discount more than the order total
        yield coupon.getDiscountValue().min(orderTotal);
      }
      case FREE_SHIPPING -> BigDecimal.ZERO; // handled at order level
    };
  }

  private void validateRequest(CouponRequest request) {
    if (request.getDiscountType() != CouponDiscountType.FREE_SHIPPING) {
      if (request.getDiscountValue() == null
          || request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("Discount value is required");
      }
      if (request.getDiscountType() == CouponDiscountType.PERCENTAGE
          && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
        throw new BadRequestException("Percentage discount cannot exceed 100%");
      }
    }
    if (request.getStartDate() != null
        && request.getEndDate() != null
        && request.getStartDate().isAfter(request.getEndDate())) {
      throw new BadRequestException("Start date cannot be after end date");
    }
  }
}
