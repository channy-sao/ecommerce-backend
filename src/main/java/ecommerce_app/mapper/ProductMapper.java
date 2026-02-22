package ecommerce_app.mapper;

import ecommerce_app.core.io.service.StaticResourceService;
import ecommerce_app.dto.response.MobileProductListResponse;
import ecommerce_app.dto.response.MobileProductResponse;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.ProductImage;
import ecommerce_app.entity.Promotion;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
            // CHANGED: use getPrimaryImagePath() instead of getImage()
            // NEW: full image list for detail/gallery view
            .images(
                product.getImages() == null
                    ? List.of()
                    : product.getImages().stream()
                        .sorted(Comparator.comparing(ProductImage::getSortOrder))
                        .map(img -> staticResourceService.getProductImageUrl(img.getImagePath()))
                        .filter(Objects::nonNull)
                        .toList())
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

    if (product.getCategory() != null) {
      response.setCategoryId(product.getCategory().getId());
      response.setCategoryName(product.getCategory().getName());
    }

    if (Boolean.TRUE.equals(product.getHasPromotion())) {
      product.getPromotions().stream()
          .filter(Promotion::getActive)
          .filter(Promotion::isCurrentlyValid)
          .max(
              Comparator.comparing(
                  p -> p.getDiscountValue() != null ? p.getDiscountValue() : BigDecimal.ZERO))
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
            // CHANGED: use getPrimaryImagePath() instead of getImage()
            .image(staticResourceService.getProductImageUrl(product.getPrimaryImagePath()))
            .isFeature(product.getIsFeature())
            .favoritesCount(product.getFavoritesCount())
            .stockQuantity(product.getStockQuantity())
            .inStock(product.getInStock())
            .stockStatus(product.getStockStatus())
            .hasPromotion(product.getHasPromotion())
            .promotionBadge(product.getPromotionBadge())
            .quickAddAvailable(product.getQuickAddAvailable())
            .build();

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
