package ecommerce_app.infrastructure.mapper;

import ecommerce_app.infrastructure.io.service.StaticResourceService;
import ecommerce_app.modules.product.model.dto.MobileProductListResponse;
import ecommerce_app.modules.product.model.dto.MobileProductResponse;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.util.Comparator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
  private final StaticResourceService staticResourceService;

  public MobileProductResponse toDetailResponse(Product product) {
    MobileProductResponse response =
        MobileProductResponse.builder()
            .id(product.getId())
            .uuid(product.getUuid())
            .name(product.getName())
            .description(product.getDescription())
            .shortDescription(product.getShortDescription())
            .price(product.getPrice())
            .discountedPrice(product.getDiscountedPrice())
            .discountPercentage(product.getDiscountPercentage())
            .image(staticResourceService.getProductImageUrl(product.getImage()))
            .isFeature(product.getIsFeature())
            .favoritesCount(product.getFavoritesCount())
            .stockQuantity(product.getStockQuantity())
            .inStock(product.getInStock())
            .stockStatus(product.getStockStatus())
            .hasPromotion(product.getHasPromotion())
            .promotionBadge(product.getPromotionBadge())
            .quickAddAvailable(product.getQuickAddAvailable())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();

    // Add category info
    if (product.getCategory() != null) {
      response.setCategoryId(product.getCategory().getId());
      response.setCategoryName(product.getCategory().getName());
    }

    // Add active promotion details
    if (Boolean.TRUE.equals(product.getHasPromotion())) {
      product.getPromotions().stream()
          .filter(Promotion::getActive)
          .filter(Promotion::isCurrentlyValid)
          .max(
              Comparator.comparing(
                  p ->
                      p.getDiscountValue() != null
                          ? p.getDiscountValue()
                          : java.math.BigDecimal.ZERO))
          .ifPresent(activePromo -> response.setActivePromotion(toPromotionDetails(activePromo)));
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
            .image(staticResourceService.getProductImageUrl(product.getImage()))
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

  private MobileProductResponse.PromotionDetails toPromotionDetails(Promotion promotion) {
    return MobileProductResponse.PromotionDetails.builder()
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
