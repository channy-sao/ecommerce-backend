package ecommerce_app.infrastructure.mapper;

import ecommerce_app.modules.product.model.dto.MobileProductListResponse;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionListResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionResponse;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.time.LocalDateTime;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ecommerce_app.constant.enums.PromotionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductMobileMapper {

  public ecommerce_app.modules.product.model.dto.response.MobileProductResponse toDetailResponse(
      Product product) {
    ecommerce_app.modules.product.model.dto.response.MobileProductResponse response =
        ecommerce_app.modules.product.model.dto.response.MobileProductResponse.builder()
            .id(product.getId())
            .uuid(product.getUuid())
            .name(product.getName())
            .description(product.getDescription())
            .shortDescription(product.getShortDescription())
            .price(product.getPrice())
            .discountedPrice(product.getDiscountedPrice())
            .discountPercentage(product.getDiscountPercentage())
            .image(product.getImage())
            .isFeature(product.getIsFeature())
            .favoritesCount(product.getFavoritesCount())
            .stockQuantity(product.getStockQuantity())
            .inStock(product.getInStock())
            .stockStatus(product.getStockStatus())
            .hasPromotion(product.getHasPromotion())
            .promotionBadge(product.getPromotionBadge())
            .quickAddAvailable(product.getQuickAddAvailable())
            .createdAt(LocalDateTime.from(product.getCreatedAt()))
            .updatedAt(LocalDateTime.from(product.getUpdatedAt()))
            .build();

    // Add category info
    if (product.getCategory() != null) {
      response.setCategoryId(product.getCategory().getId());
      response.setCategoryName(product.getCategory().getName());
    }

    // Add active promotion details
    if (Boolean.TRUE.equals(product.getHasPromotion())) {
      Promotion activePromo =
          product.getPromotions().stream()
              .filter(Promotion::getActive)
              .filter(Promotion::isCurrentlyValid)
              .max(
                  Comparator.comparing(
                      p ->
                          p.getDiscountValue() != null
                              ? p.getDiscountValue()
                              : java.math.BigDecimal.ZERO))
              .orElse(null);

      if (activePromo != null) {
        response.setActivePromotion(toPromotionDetails(activePromo));
      }
    }

    return response;
  }

  public MobileProductListResponse toListResponse(Product product) {
    MobileProductListResponse response =
        MobileProductListResponse.builder()
            .id(product.getId())
            .uuid(product.getUuid())
            .name(product.getName())
            .shortDescription(product.getShortDescription())
            .price(product.getPrice())
            .discountedPrice(product.getDiscountedPrice())
            .discountPercentage(product.getDiscountPercentage())
            .image(product.getImage())
            .isFeature(product.getIsFeature())
            .favoritesCount(product.getFavoritesCount())
            .stockQuantity(product.getStockQuantity())
            .inStock(product.getInStock())
            .stockStatus(product.getStockStatus())
            .hasPromotion(product.getHasPromotion())
            .promotionBadge(product.getPromotionBadge())
            .quickAddAvailable(product.getQuickAddAvailable())
            .build();

    // Add category info
    if (product.getCategory() != null) {
      response.setCategoryId(product.getCategory().getId());
      response.setCategoryName(product.getCategory().getName());
    }

    return response;
  }

  private ecommerce_app.modules.product.model.dto.response.MobileProductResponse.PromotionDetails
      toPromotionDetails(Promotion promotion) {
    return ecommerce_app.modules.product.model.dto.response.MobileProductResponse.PromotionDetails
        .builder()
        .id(promotion.getId())
        .code(promotion.getCode())
        .name(promotion.getName())
        .discountType(promotion.getDiscountType().name())
        .discountValue(promotion.getDiscountValue())
        .buyQuantity(promotion.getBuyQuantity())
        .getQuantity(promotion.getGetQuantity())
        .startAt(promotion.getStartAt())
        .endAt(promotion.getEndAt())
        .minPurchaseAmount(promotion.getMinPurchaseAmount())
        .remainingUsage(promotion.getRemainingUsage())
        .build();
  }
}
