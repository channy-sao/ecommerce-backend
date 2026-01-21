// ecommerce_app/modules/promotion/facade/PromotionFacade.java
package ecommerce_app.modules.promotion.facade;

import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import ecommerce_app.modules.promotion.repository.PromotionRepository;
import ecommerce_app.modules.promotion.strategy.PromotionStrategy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionFacade {

  private final PromotionRepository promotionRepository;
  private final Map<String, PromotionStrategy> strategies;

  public BigDecimal calculateDiscount(
      String strategyType, Promotion promotion, Product product, Integer quantity) {
    // 1. Get strategy
    PromotionStrategy strategy = strategies.get(strategyType);

    if (strategy == null) {
      throw new IllegalArgumentException("Unsupported promotion strategy: " + strategyType);
    }

    // 2. Validate promotion status (facade orchestration)
    validatePromotion(promotion);

    // 3. Validate promotion applicability
    validateApplicability(promotion, product);

    // 4. Let strategy validate its specific rules
    strategy.validate(promotion);

    // 5. Calculate discount
    BigDecimal discount = strategy.calculateDiscount(promotion, product, quantity);

    // 6. Log the operation
    logDiscountCalculation(strategyType, promotion, product, quantity, discount);

    return discount;
  }

  public BigDecimal calculateDiscount(Promotion promotion, Product product, Integer quantity) {
    // Auto-detect strategy type from promotion
    String strategyType = mapPromotionTypeToStrategy(promotion);
    return calculateDiscount(strategyType, promotion, product, quantity);
  }

  private void validatePromotion(Promotion promotion) {
    if (promotion == null) {
      throw new IllegalArgumentException("Promotion cannot be null");
    }

    if (Boolean.FALSE.equals(promotion.getActive())) {
      throw new IllegalStateException("Promotion is not active");
    }

    LocalDateTime now = LocalDateTime.now();

    if (promotion.getStartAt() != null && now.isBefore(promotion.getStartAt())) {
      throw new IllegalStateException("Promotion has not started yet");
    }

    if (promotion.getEndAt() != null && now.isAfter(promotion.getEndAt())) {
      throw new IllegalStateException("Promotion has expired");
    }

    // Check usage limits
    if (promotion.getMaxUsage() != null && promotion.getUsages() != null) {
      if (promotion.getUsages().size() >= promotion.getMaxUsage()) {
        throw new IllegalStateException("Promotion usage limit reached");
      }
    }
  }

  private void validateApplicability(Promotion promotion, Product product) {
    // Check if promotion applies to this product
    if (promotion.getProducts() != null && !promotion.getProducts().isEmpty()) {
      boolean isApplicable =
          promotion.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId()));

      if (!isApplicable) {
        throw new IllegalStateException("Promotion does not apply to this product");
      }
    }
  }

  private String mapPromotionTypeToStrategy(Promotion promotion) {
    return switch (promotion.getDiscountType()) {
      case PERCENTAGE -> "percentage";
      case FIXED_AMOUNT -> "fixed";
      case BUY_X_GET_Y -> "buyxgety";
      case FREE_SHIPPING -> "freeshipping";
    };
  }

  public Promotion getPromotionByCode(String promotionCode) {
    return promotionRepository.findByCode(promotionCode).orElse(null);
  }

  public List<Promotion> getAvailablePromotion(Product product) {
    return promotionRepository.findActivePromotionsByProductId(
        product.getId(), LocalDateTime.now());
  }

  public boolean isPromotionActive(Promotion promotion) {
    if (promotion == null || !promotion.getActive()) {
      return false;
    }

    LocalDateTime now = LocalDateTime.now();

    if (promotion.getStartAt() != null && now.isBefore(promotion.getStartAt())) {
      return false;
    }

    return promotion.getEndAt() == null || !now.isAfter(promotion.getEndAt());
  }

  public boolean isProductEligibleForPromotion(Product product, Promotion promotion) {
    // If promotion has no specific products, it applies to all
    if (promotion.getProducts() == null || promotion.getProducts().isEmpty()) {
      return true;
    }

    // Check if this product is in the promotion's product list
    return promotion.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId()));
  }

  private void logDiscountCalculation(
      String strategyType,
      Promotion promotion,
      Product product,
      Integer quantity,
      BigDecimal discount) {
    log.info(
        "Discount calculated - Strategy: {}, Promotion: {}, Product: {}, Quantity: {}, Discount: {}",
        strategyType,
        promotion.getCode(),
        product.getId(),
        quantity,
        discount);
  }

  // Helper method to list available strategies
  public Map<String, String> getAvailableStrategies() {
    return strategies.entrySet().stream()
        .collect(
            java.util.stream.Collectors.toMap(
                Map.Entry::getKey, entry -> entry.getValue().getClass().getSimpleName()));
  }
}
