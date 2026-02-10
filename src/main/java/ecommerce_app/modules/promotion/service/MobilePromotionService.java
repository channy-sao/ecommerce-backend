package ecommerce_app.modules.promotion.service;

import ecommerce_app.modules.promotion.model.dto.MobilePromotionListResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionValidationRequest;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionValidationResponse;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mobile Promotion Service Interface
 *
 * <p>Dedicated service for mobile app promotion operations
 * Handles promotion viewing, validation, and mobile-specific queries
 */
public interface MobilePromotionService {

    /**
     * Get all active promotions for mobile app
     * Returns promotions that are currently valid and active
     *
     * @param pageable Pagination and sorting
     * @return Paginated list of active promotions
     */
    Page<MobilePromotionListResponse> getActivePromotions(Pageable pageable);

    /**
     * Get promotion by ID
     * Used when user taps on a promotion to see details
     *
     * @param id Promotion ID
     * @return Promotion entity
     */
    MobilePromotionResponse getPromotionById(Long id);

    /**
     * Get promotion by code
     * Used when user sees a promo code in marketing and searches for it
     *
     * @param code Promotion code
     * @return Promotion entity
     */
    MobilePromotionResponse getPromotionByCode(String code);

    /**
     * Get upcoming promotions
     * Shows "Coming Soon" promotions in the app
     *
     * @param limit Maximum number of promotions
     * @return List of upcoming promotions
     */
    List<MobilePromotionListResponse> getUpcomingPromotions(int limit);

    /**
     * Get best/featured promotions
     * Shows highest discount promotions on homepage
     *
     * @param limit Maximum number of promotions
     * @return List of featured promotions
     */
    List<MobilePromotionListResponse> getFeaturedPromotions(int limit);

    /**
     * Validate promotion code for cart
     *
     * <p>This is the MAIN method for mobile checkout flow
     * Validates if a promo code can be applied to the user's cart
     *
     * @param request Contains code, userId, and cartTotal
     * @return Validation response with discount calculation or error message
     */
    MobilePromotionValidationResponse validatePromotionCode(MobilePromotionValidationRequest request);

    /**
     * Validate promotion code (internal method)
     *
     * <p>Performs 7 validation checks:
     * 1. Code exists
     * 2. Promotion is active
     * 3. Current date is within valid range
     * 4. Cart total meets minimum purchase
     * 5. Max usage limit not reached
     * 6. User hasn't exceeded per-user limit
     * 7. Calculate discount amount
     *
     * @param code Promotion code
     * @param userId User ID
     * @param cartTotal Cart total amount
     * @return Validation response
     */
    MobilePromotionValidationResponse validatePromotion(String code, Long userId, BigDecimal cartTotal);

    /**
     * Check if user can use a specific promotion
     * Useful for showing "You can use this" badges in the app
     *
     * @param promotionId Promotion ID
     * @param userId User ID
     * @param cartTotal Cart total amount
     * @return true if user can use the promotion
     */
    boolean canUserUsePromotion(Long promotionId, Long userId, BigDecimal cartTotal);

    /**
     * Get promotion display badge text
     * For showing in product cards or promotion banners
     *
     * @param promotion Promotion entity
     * @return Badge text or null
     */
    String getPromotionBadge(Promotion promotion);
}