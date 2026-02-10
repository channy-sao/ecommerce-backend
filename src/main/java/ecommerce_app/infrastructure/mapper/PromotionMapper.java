package ecommerce_app.infrastructure.mapper;

import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionListResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionResponse;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromotionMapper {

  public MobilePromotionResponse toDetailResponse(Promotion promotion) {
    MobilePromotionResponse response =
        MobilePromotionResponse.builder()
            .id(promotion.getId())
            .code(promotion.getCode())
            .name(promotion.getName())
            .discountType(promotion.getDiscountType().name())
            .discountValue(promotion.getDiscountValue())
            .buyQuantity(promotion.getBuyQuantity())
            .getQuantity(promotion.getGetQuantity())
            .active(promotion.getActive())
            .startAt(promotion.getStartAt())
            .endAt(promotion.getEndAt())
            .maxUsage(promotion.getMaxUsage())
            .maxUsagePerUser(promotion.getMaxUsagePerUser())
            .minPurchaseAmount(promotion.getMinPurchaseAmount())
            .isCurrentlyValid(promotion.isCurrentlyValid())
            .remainingUsage(promotion.getRemainingUsage())
            .status(determineStatus(promotion))
            .createdAt(LocalDateTime.from(promotion.getCreatedAt()))
            .updatedAt(LocalDateTime.from(promotion.getUpdatedAt()))
            .createdBy(String.valueOf(promotion.getCreatedBy()))
            .updatedBy(String.valueOf(promotion.getUpdatedBy()))
            .build();

    // Add applicable products
    if (promotion.getProducts() != null && !promotion.getProducts().isEmpty()) {
      List<MobilePromotionResponse.ProductSummary> productSummaries =
          promotion.getProducts().stream().map(this::toProductSummary).collect(Collectors.toList());
      response.setApplicableProducts(productSummaries);
    }

    return response;
  }

  public MobilePromotionListResponse toListResponse(Promotion promotion) {
    return MobilePromotionListResponse.builder()
        .id(promotion.getId())
        .code(promotion.getCode())
        .name(promotion.getName())
        .discountType(promotion.getDiscountType().name())
        .discountValue(promotion.getDiscountValue())
        .buyQuantity(promotion.getBuyQuantity())
        .getQuantity(promotion.getGetQuantity())
        .active(promotion.getActive())
        .startAt(promotion.getStartAt())
        .endAt(promotion.getEndAt())
        .minPurchaseAmount(promotion.getMinPurchaseAmount())
        .displayText(getDisplayText(promotion))
        .status(determineStatus(promotion))
        .isCurrentlyValid(promotion.isCurrentlyValid())
        .remainingUsage(promotion.getRemainingUsage())
        .applicableProductsCount(
            promotion.getProducts() != null ? promotion.getProducts().size() : 0)
        .build();
  }

  private MobilePromotionResponse.ProductSummary toProductSummary(Product product) {
    return MobilePromotionResponse.ProductSummary.builder()
        .id(product.getId())
        .name(product.getName())
        .image(product.getImage())
        .price(product.getPrice())
        .discountedPrice(product.getDiscountedPrice())
        .build();
  }

  private String determineStatus(Promotion promotion) {
    if (Boolean.FALSE.equals(promotion.getActive())) {
      return "INACTIVE";
    }

    LocalDateTime now = LocalDateTime.now();

    if (promotion.getStartAt() != null && now.isBefore(promotion.getStartAt())) {
      return "UPCOMING";
    }

    if (promotion.getEndAt() != null && now.isAfter(promotion.getEndAt())) {
      return "EXPIRED";
    }

    if (promotion.hasReachedMaxUsage()) {
      return "EXPIRED";
    }

    return "ACTIVE";
  }

  private String getDisplayText(Promotion promotion) {
    PromotionType type = promotion.getDiscountType();

    switch (type) {
      case PERCENTAGE:
        if (promotion.getDiscountValue() != null) {
          return promotion.getDiscountValue().intValue() + "% OFF";
        }
        break;
      case FIXED_AMOUNT:
        if (promotion.getDiscountValue() != null) {
          return "$" + promotion.getDiscountValue() + " OFF";
        }
        break;
      case BUY_X_GET_Y:
        if (promotion.getBuyQuantity() != null && promotion.getGetQuantity() != null) {
          return "Buy "
              + promotion.getBuyQuantity()
              + " Get "
              + promotion.getGetQuantity()
              + " Free";
        }
        break;
      case FREE_SHIPPING:
        return "Free Shipping";
      default:
        break;
    }

    return "Special Offer";
  }
}
