package ecommerce_app.modules.promotion.service.impl;

import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.mapper.PromotionMapper;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionListResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionValidationRequest;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionValidationResponse;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import ecommerce_app.modules.promotion.repository.PromotionRepository;
import ecommerce_app.modules.promotion.repository.PromotionUsageRepository;
import ecommerce_app.modules.promotion.service.MobilePromotionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mobile Promotion Service
 *
 * <p>Dedicated service for mobile app promotion operations Handles promotion viewing, validation,
 * and mobile-specific queries
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MobilePromotionServiceImpl implements MobilePromotionService {

  private final PromotionRepository promotionRepository;
  private final PromotionUsageRepository promotionUsageRepository;
  private final PromotionMapper promotionMapper;

  /**
   * Get all active promotions for mobile app Returns promotions that are currently valid and active
   */
  public Page<MobilePromotionListResponse> getActivePromotions(Pageable pageable) {
    LocalDateTime now = LocalDateTime.now();
    var promotionPage = promotionRepository.findActivePromotions(now, pageable);
    return promotionPage.map(promotionMapper::toListResponse);
  }

  /** Get promotion by ID Used when user taps on a promotion to see details */
  public MobilePromotionResponse getPromotionById(Long id) {
    final var promotion =
        promotionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion" + id));
    return promotionMapper.toDetailResponse(promotion);
  }

  /** Get promotion by code Used when user sees a promo code in marketing and searches for it */
  public MobilePromotionResponse getPromotionByCode(String code) {
    final var promotion =
        promotionRepository
            .findByCode(code)
            .orElseThrow(
                () -> new ResourceNotFoundException("Promotion not found with code: " + code));
    return promotionMapper.toDetailResponse(promotion);
  }

  /** Get upcoming promotions Shows "Coming Soon" promotions in the app */
  public List<MobilePromotionListResponse> getUpcomingPromotions(int limit) {
    LocalDateTime now = LocalDateTime.now();
    Pageable pageable = PageRequest.of(0, limit);
    return promotionRepository.findUpcomingPromotions(now, pageable).getContent().stream()
        .map(promotionMapper::toListResponse)
        .toList();
  }

  /** Get best/featured promotions Shows highest discount promotions on homepage */
  public List<MobilePromotionListResponse> getFeaturedPromotions(int limit) {
    LocalDateTime now = LocalDateTime.now();
    Pageable pageable = PageRequest.of(0, limit);
    return promotionRepository.findBestPromotions(now, pageable).getContent().stream()
        .map(promotionMapper::toListResponse)
        .toList();
  }

  /**
   * Validate promotion code for cart
   *
   * <p>This is the MAIN method for mobile checkout flow Validates if a promo code can be applied to
   * the user's cart
   *
   * @param request Contains code, userId, and cartTotal
   * @return Validation response with discount calculation or error message
   */
  public MobilePromotionValidationResponse validatePromotionCode(
      MobilePromotionValidationRequest request) {

    return validatePromotion(request.getCode(), request.getUserId(), request.getCartTotal());
  }

  /**
   * Validate promotion code (internal method)
   *
   * <p>Performs 7 validation checks: 1. Code exists 2. Promotion is active 3. Current date is
   * within valid range 4. Cart total meets minimum purchase 5. Max usage limit not reached 6. User
   * hasn't exceeded per-user limit 7. Calculate discount amount
   */
  public MobilePromotionValidationResponse validatePromotion(
      String code, Long userId, BigDecimal cartTotal) {

    // 1. Find promotion by code
    Promotion promotion;
    try {
      promotion =
          promotionRepository
              .findByCode(code)
              .orElseThrow(
                  () -> new ResourceNotFoundException("Promotion not found with code: " + code));
    } catch (ResourceNotFoundException _) {
      return MobilePromotionValidationResponse.invalid(
          "Invalid promotion code. Please check and try again.");
    }

    // 2. Check if promotion is active
    if (Boolean.FALSE.equals(promotion.getActive())) {
      return MobilePromotionValidationResponse.invalid("This promotion is currently inactive.");
    }

    // 3. Check if currently valid (date range)
    if (!promotion.isCurrentlyValid()) {
      LocalDateTime now = LocalDateTime.now();

      // Not yet started
      if (promotion.getStartAt() != null && now.isBefore(promotion.getStartAt())) {
        return MobilePromotionValidationResponse.invalid(
            "This promotion starts on " + formatDate(promotion.getStartAt()));
      }

      // Already expired
      if (promotion.getEndAt() != null && now.isAfter(promotion.getEndAt())) {
        return MobilePromotionValidationResponse.invalid("This promotion has expired.");
      }
    }

    // 4. Check minimum purchase amount
    if (promotion.getMinPurchaseAmount() != null
        && cartTotal.compareTo(promotion.getMinPurchaseAmount()) < 0) {

      BigDecimal needed = promotion.getMinPurchaseAmount().subtract(cartTotal);
      return MobilePromotionValidationResponse.invalid(
          String.format(
              "Add $%.2f more to use this code. Minimum purchase: $%.2f",
              needed, promotion.getMinPurchaseAmount()));
    }

    // 5. Check global max usage
    if (promotion.hasReachedMaxUsage()) {
      return MobilePromotionValidationResponse.invalid(
          "This promotion has reached its maximum usage limit.");
    }

    // 6. Check user usage limit
    if (promotion.getMaxUsagePerUser() != null && userId != null) {
      long userUsageCount =
          promotionUsageRepository.countByPromotionIdAndUserId(promotion.getId(), userId);

      if (userUsageCount >= promotion.getMaxUsagePerUser()) {
        return MobilePromotionValidationResponse.invalid(
            String.format(
                "You have already used this code. Maximum %d use%s per customer.",
                promotion.getMaxUsagePerUser(), promotion.getMaxUsagePerUser() > 1 ? "s" : ""));
      }
    }

    // 7. Calculate discount
    BigDecimal discountAmount = calculateDiscount(promotion, cartTotal);
    BigDecimal finalAmount = cartTotal.subtract(discountAmount);

    // Ensure final amount is never negative
    if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
      finalAmount = BigDecimal.ZERO;
    }

    // Build promotion details for response
    MobilePromotionValidationResponse.PromotionDetails promotionDetails =
        buildPromotionDetails(promotion);

    // Return success response
    String message = buildSuccessMessage(promotion, discountAmount);
    return MobilePromotionValidationResponse.valid(
        message, promotionDetails, discountAmount, finalAmount);
  }

  /** Calculate discount amount based on promotion type */
  private BigDecimal calculateDiscount(Promotion promotion, BigDecimal cartTotal) {
    PromotionType type = promotion.getDiscountType();

    switch (type) {
      case PERCENTAGE:
        if (promotion.getDiscountValue() != null) {
          // Calculate percentage discount
          // Example: $1,500 × 10% = $150
          return cartTotal
              .multiply(promotion.getDiscountValue())
              .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        break;

      case FIXED_AMOUNT:
        if (promotion.getDiscountValue() != null) {
          // Don't discount more than cart total
          // Example: $50 off, but cart is only $30, discount = $30
          return promotion.getDiscountValue().min(cartTotal);
        }
        break;

      case BUY_X_GET_Y:
        // For BUY_X_GET_Y, discount calculation requires item-level data
        // This is a simplified version - you may need to pass cart items
        // For now, return 0 as this requires more complex logic
        return BigDecimal.ZERO;

      case FREE_SHIPPING:
        // Free shipping doesn't affect cart subtotal
        // The shipping cost would be handled separately in checkout
        return BigDecimal.ZERO;

      default:
        return BigDecimal.ZERO;
    }

    return BigDecimal.ZERO;
  }

  /** Build promotion details for response */
  private MobilePromotionValidationResponse.PromotionDetails buildPromotionDetails(
      Promotion promotion) {

    return MobilePromotionValidationResponse.PromotionDetails.builder()
        .id(promotion.getId())
        .code(promotion.getCode())
        .name(promotion.getName())
        .discountType(promotion.getDiscountType().name())
        .discountValue(promotion.getDiscountValue())
        .buyQuantity(promotion.getBuyQuantity())
        .getQuantity(promotion.getGetQuantity())
        .minPurchaseAmount(promotion.getMinPurchaseAmount())
        .build();
  }

  /** Build success message based on promotion type */
  private String buildSuccessMessage(Promotion promotion, BigDecimal discountAmount) {
    PromotionType type = promotion.getDiscountType();

    return switch (type) {
      case PERCENTAGE, FIXED_AMOUNT ->
          String.format("✓ %s applied! You save $%.2f", promotion.getCode(), discountAmount);
      case BUY_X_GET_Y ->
          String.format(
              "✓ %s applied! Buy %d, Get %d Free",
              promotion.getCode(), promotion.getBuyQuantity(), promotion.getGetQuantity());
      case FREE_SHIPPING ->
          String.format("✓ %s applied! Free shipping on this order", promotion.getCode());
      default -> String.format("✓ %s applied successfully!", promotion.getCode());
    };
  }

  /** Format date for user-friendly display */
  private String formatDate(LocalDateTime dateTime) {
    // Format: "January 15, 2025"
    return dateTime.toLocalDate().toString();
  }

  /**
   * Check if user can use a specific promotion Useful for showing "You can use this" badges in the
   * app
   */
  public boolean canUserUsePromotion(Long promotionId, Long userId, BigDecimal cartTotal) {
    try {
      Promotion promotion =
          promotionRepository
              .findById(promotionId)
              .orElseThrow(() -> new ResourceNotFoundException("Promotion", promotionId));

      // Quick validation without full response
      if (Boolean.FALSE.equals(promotion.getActive())) return false;
      if (!promotion.isCurrentlyValid()) return false;

      if (promotion.getMinPurchaseAmount() != null
          && cartTotal.compareTo(promotion.getMinPurchaseAmount()) < 0) {
        return false;
      }

      if (promotion.hasReachedMaxUsage()) return false;

      if (promotion.getMaxUsagePerUser() != null && userId != null) {
        long userUsageCount =
            promotionUsageRepository.countByPromotionIdAndUserId(promotionId, userId);
        return userUsageCount < promotion.getMaxUsagePerUser();
      }

      return true;

    } catch (EntityNotFoundException _) {
      return false;
    }
  }

  /** Get promotion display badge text For showing in product cards or promotion banners */
  public String getPromotionBadge(Promotion promotion) {
    if (promotion == null || Boolean.FALSE.equals(promotion.getActive())) {
      return null;
    }

    PromotionType type = promotion.getDiscountType();

    switch (type) {
      case PERCENTAGE:
        if (promotion.getDiscountValue() != null) {
          return promotion.getDiscountValue().intValue() + "% OFF";
        }
        break;

      case FIXED_AMOUNT:
        if (promotion.getDiscountValue() != null) {
          return "$" + promotion.getDiscountValue().intValue() + " OFF";
        }
        break;

      case BUY_X_GET_Y:
        if (promotion.getBuyQuantity() != null && promotion.getGetQuantity() != null) {
          return "BUY "
              + promotion.getBuyQuantity()
              + " GET "
              + promotion.getGetQuantity()
              + " FREE";
        }
        break;

      case FREE_SHIPPING:
        return "FREE SHIPPING";

      default:
        return "SPECIAL OFFER";
    }

    return "SALE";
  }
}
