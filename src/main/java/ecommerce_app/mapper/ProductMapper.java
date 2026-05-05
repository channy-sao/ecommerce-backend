package ecommerce_app.mapper;

import ecommerce_app.core.io.service.StaticResourceService;
import ecommerce_app.dto.response.MobileProductListResponse;
import ecommerce_app.dto.response.MobileProductResponse;
import ecommerce_app.dto.response.SimpleBrandResponse;
import ecommerce_app.dto.response.WarrantyResponse;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.ProductAttributeValue;
import ecommerce_app.entity.ProductImage;
import ecommerce_app.entity.ProductVariant;
import ecommerce_app.entity.Promotion;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ecommerce_app.util.PromotionCalculator;
import ecommerce_app.util.WarrantyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
  private final StaticResourceService staticResourceService;
  private final ProductVariantMapper variantMapper;

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
            .hasVariants(product.getHasVariants())
            .activeVariantCount(getActiveVariantCount(product))
            .defaultVariantId(getDefaultVariantId(product))
            .variants(mapToMobileVariants(product))
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
            .stockQuantity(product.getTotalStock())
            .inStock(product.getInStock())
            .stockStatus(product.getStockStatus())
            .hasPromotion(product.getHasPromotion())
            .promotionBadge(promotionBadge)
            .quickAddAvailable(product.getQuickAddAvailable())
            .hasVariants(product.getHasVariants())
            .activeVariantCount(getActiveVariantCount(product))
            .minPrice(getMinVariantPrice(product))
            .maxPrice(getMaxVariantPrice(product))
            .defaultVariantSku(getDefaultVariantSku(product))
            .defaultVariantId(getDefaultVariantId(product))
            .variantOptions(buildVariantOptionGroups(product))
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

  /** Map product variants to mobile variant responses for detail page. */
  private List<MobileProductResponse.MobileVariantResponse> mapToMobileVariants(Product product) {
    if (product.getVariants() == null || product.getVariants().isEmpty()) {
      return List.of();
    }

    return product.getVariants().stream()
        .map(
            variant ->
                MobileProductResponse.MobileVariantResponse.builder()
                    .id(variant.getId())
                    .sku(variant.getSku())
                    .price(variant.getPrice())
                    .effectivePrice(variant.getEffectivePrice())
                    .stockQuantity(variant.getStockQuantity())
                    .inStock(variant.getInStock())
                    .stockStatus(variant.getStockStatus())
                    .isDefault(variant.getIsDefault())
                    .isActive(variant.getIsActive())
                    .lowStockThreshold(variant.getLowStockThreshold())
                    .attributeValues(mapAttributeValues(variant))
                    .build())
        .toList();
  }

  /** Map attribute values for a variant. */
  private List<MobileProductResponse.AttributeValueDto> mapAttributeValues(ProductVariant variant) {
    if (variant.getAttributeValues() == null || variant.getAttributeValues().isEmpty()) {
      return List.of();
    }

    return variant.getAttributeValues().stream()
        .map(
            attr ->
                MobileProductResponse.AttributeValueDto.builder()
                    .id(attr.getId())
                    .attribute(
                        attr.getProductAttribute() != null
                            ? attr.getProductAttribute().getName()
                            : null)
                    .value(attr.getValue())
                    .build())
        .toList();
  }

  /** Build grouped variant options for mobile listing (e.g., Color: [Red, Blue, Black]). */
  private List<MobileProductListResponse.VariantOptionGroup> buildVariantOptionGroups(
      Product product) {
    if (!Boolean.TRUE.equals(product.getHasVariants()) || product.getVariants() == null) {
      return List.of();
    }

    // Group attribute values by attribute name
    Map<String, Map<String, Boolean>> grouped = new LinkedHashMap<>();

    for (ProductVariant variant : product.getVariants()) {
      if (!Boolean.TRUE.equals(variant.getIsActive())) continue;

      for (ProductAttributeValue attr : variant.getAttributeValues()) {
        String attrName =
            attr.getProductAttribute() != null ? attr.getProductAttribute().getName() : "Unknown";
        String value = attr.getValue();

        grouped
            .computeIfAbsent(attrName, k -> new LinkedHashMap<>())
            .merge(value, variant.getInStock(), (existing, incoming) -> existing || incoming);
      }
    }

    return grouped.entrySet().stream()
        .map(
            entry -> {
              List<MobileProductListResponse.VariantOptionValue> values =
                  entry.getValue().entrySet().stream()
                      .map(
                          valEntry ->
                              MobileProductListResponse.VariantOptionValue.builder()
                                  .value(valEntry.getKey())
                                  .inStock(valEntry.getValue())
                                  .build())
                      .toList();

              return MobileProductListResponse.VariantOptionGroup.builder()
                  .name(entry.getKey())
                  .values(values)
                  .build();
            })
        .toList();
  }

  /** Count active variants for a product. */
  private int getActiveVariantCount(Product product) {
    if (product.getVariants() == null) return 0;
    return (int) product.getVariants().stream().filter(ProductVariant::getIsActive).count();
  }

  /** Get the default variant ID. */
  private Long getDefaultVariantId(Product product) {
    ProductVariant defaultVariant = product.getDefaultVariant();
    return defaultVariant != null ? defaultVariant.getId() : null;
  }

  /** Get the default variant SKU. */
  private String getDefaultVariantSku(Product product) {
    ProductVariant defaultVariant = product.getDefaultVariant();
    return defaultVariant != null ? defaultVariant.getSku() : null;
  }

  /** Get the minimum price among all active variants. */
  private BigDecimal getMinVariantPrice(Product product) {
    if (product.getVariants() == null || product.getVariants().isEmpty()) {
      return product.getPrice();
    }
    return product.getVariants().stream()
        .filter(ProductVariant::getIsActive)
        .map(ProductVariant::getEffectivePrice)
        .filter(Objects::nonNull)
        .min(BigDecimal::compareTo)
        .orElse(product.getPrice());
  }

  /** Get the maximum price among all active variants. */
  private BigDecimal getMaxVariantPrice(Product product) {
    if (product.getVariants() == null || product.getVariants().isEmpty()) {
      return product.getPrice();
    }
    return product.getVariants().stream()
        .filter(ProductVariant::getIsActive)
        .map(ProductVariant::getEffectivePrice)
        .filter(Objects::nonNull)
        .max(BigDecimal::compareTo)
        .orElse(product.getPrice());
  }
}
