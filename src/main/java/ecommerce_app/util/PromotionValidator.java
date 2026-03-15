package ecommerce_app.util;

import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.dto.request.PromotionRequest;
import ecommerce_app.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PromotionValidator {

  public void validate(PromotionRequest request) {

    // PERCENTAGE discount must be between 0 and 100
    if (request.getDiscountType() == PromotionType.PERCENTAGE) {
      if (request.getDiscountValue() == null) {
        throw new BadRequestException("Discount value is required for PERCENTAGE type");
      }
      if (request.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
        throw new BadRequestException("Percentage discount must not exceed 100");
      }
    }

    // FIXED_AMOUNT must have a discount value
    if (request.getDiscountType() == PromotionType.FIXED_AMOUNT
        && request.getDiscountValue() == null) {
      throw new BadRequestException("Discount value is required for FIXED_AMOUNT type");
    }

    // BUY_X_GET_Y must have both quantities
    if (request.getDiscountType() == PromotionType.BUY_X_GET_Y) {
      if (request.getBuyQuantity() == null || request.getBuyQuantity() <= 0) {
        throw new BadRequestException("Buy quantity is required and must be greater than 0 for BUY_X_GET_Y type");
      }
      if (request.getGetQuantity() == null || request.getGetQuantity() <= 0) {
        throw new BadRequestException("Get quantity is required and must be greater than 0 for BUY_X_GET_Y type");
      }
    }

    // End date must be after start date
    if (request.getStartAt() != null && request.getEndAt() != null
        && request.getEndAt().isBefore(request.getStartAt())) {
      throw new BadRequestException("End date must be after start date");
    }

    // If not applyToAll, productIds must be provided
    if (!request.isApplyToAll()
        && (request.getProductIds() == null || request.getProductIds().isEmpty())) {
      throw new BadRequestException("Product IDs are required when promotion does not apply to all products");
    }
  }
}