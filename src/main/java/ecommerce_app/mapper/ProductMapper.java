package ecommerce_app.mapper;

import ecommerce_app.core.io.service.StaticResourceService;
import ecommerce_app.dto.response.MobileProductListResponse;
import ecommerce_app.dto.response.MobileProductResponse;
import ecommerce_app.dto.response.SimpleBrandResponse;
import ecommerce_app.dto.response.WarrantyResponse;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.ProductImage;
import ecommerce_app.entity.Promotion;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import ecommerce_app.util.PromotionCalculator;
import ecommerce_app.util.WarrantyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
  private final StaticResourceService staticResourceService;

  public MobileProductResponse toDetailResponse(Product product) {
    final var discountPrice =
        PromotionCalculator.calculateDiscountedPrice(product.getPrice(), product.getPromotions());
    final var discountPercentage =
        PromotionCalculator.calculateDiscountPercentage(product.getPrice(), discountPrice);
    final var promotionBadge =
        PromotionCalculator.buildPromotionBadge(
            product.getPrice(), product.getPromotions(), discountPercentage);
    MobileProductResponse response =
        MobileProductResponse.builder()
            .id(product.getId())
            .uuid(product.getUuid())
            .code(product.getCode())
            .name(product.getName())
            .description(product.getDescription())
            .shortDescription(product.getShortDescription())
            .price(product.getPrice())
            .discountedPrice(discountPrice)
            .discountPercentage(discountPercentage)
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
            .promotionBadge(promotionBadge)
            .quickAddAvailable(product.getQuickAddAvailable())
            .warranty(getWarranty(product))
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
    response.setBrand(resolveBrand(product));

    return response;
  }

  public MobileProductListResponse toListResponse(Product product) {
    final var discountPrice =
        PromotionCalculator.calculateDiscountedPrice(product.getPrice(), product.getPromotions());
    final var discountPercentage =
        PromotionCalculator.calculateDiscountPercentage(product.getPrice(), discountPrice);
    final var promotionBadge =
        PromotionCalculator.buildPromotionBadge(
            product.getPrice(), product.getPromotions(), discountPercentage);
    MobileProductListResponse response =
        MobileProductListResponse.builder()
            .id(product.getId())
            .uuid(product.getUuid())
            .code(product.getCode())
            .name(product.getName())
            .shortDescription(product.getShortDescription())
            .price(product.getPrice())
            .discountedPrice(discountPrice)
            .discountPercentage(discountPercentage)
            // CHANGED: use getPrimaryImagePath() instead of getImage()
            .image(staticResourceService.getProductImageUrl(product.getPrimaryImagePath()))
            .isFeature(product.getIsFeature())
            .favoritesCount(product.getFavoritesCount())
            .stockQuantity(product.getStockQuantity())
            .inStock(product.getInStock())
            .stockStatus(product.getStockStatus())
            .hasPromotion(product.getHasPromotion())
            .promotionBadge(promotionBadge)
            .quickAddAvailable(product.getQuickAddAvailable())
            .build();

    if (product.getCategory() != null) {
      response.setCategoryId(product.getCategory().getId());
      response.setCategoryName(product.getCategory().getName());
    }
    response.setBrand(resolveBrand(product));

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

  private WarrantyResponse getWarranty(Product product) {
    return WarrantyResponse.builder()
        .type(product.getWarrantyType())
        .description(product.getWarrantyDescription())
        .unit(product.getWarrantyUnit())
        .duration(product.getWarrantyDuration())
        .label(
            WarrantyUtil.buildLabel(
                product.getWarrantyType(),
                product.getWarrantyDuration(),
                product.getWarrantyUnit()))
        .build();
  }

  private SimpleBrandResponse resolveBrand(Product product) {
    if (product.getBrand() == null) return null;

    return SimpleBrandResponse.builder()
        .id(product.getBrand().getId())
        .name(product.getBrand().getName())
        .logo(
            product.getBrand().getLogo() != null
                ? staticResourceService.getLogoUrl(product.getBrand().getLogo())
                : null)
        .build();
  }
}
