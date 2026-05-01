package ecommerce_app.util;

import ecommerce_app.core.io.service.FileManagerService;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.dto.response.ProductImageResponse;
import ecommerce_app.dto.response.ProductResponse;
import ecommerce_app.dto.response.SimpleBrandResponse;
import ecommerce_app.dto.response.SimpleProductResponse;
import ecommerce_app.dto.response.WarrantyResponse;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.ProductImage;
import ecommerce_app.mapper.ProductVariantMapper;
import ecommerce_app.property.StorageConfigProperty;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductMapper {

  private static ModelMapper modelMapper;
  private static FileManagerService fileManagerService;
  private static ProductVariantMapper variantMapper;
  private static StorageConfigProperty storageConfigProperty;
  private static AuditUserResolver auditUserResolver;
  private static StorageConfig storageConfig;

  public static void setProperties(
      ModelMapper modelMapper,
      FileManagerService fileManagerService,
      StorageConfigProperty storageConfigProperty,
      StorageConfig storageConfig,
      AuditUserResolver auditUserResolver,
      ProductVariantMapper variantMapper) {
    ProductMapper.modelMapper = modelMapper;
    ProductMapper.fileManagerService = fileManagerService;
    ProductMapper.storageConfigProperty = storageConfigProperty;
    ProductMapper.storageConfig = storageConfig;
    ProductMapper.auditUserResolver = auditUserResolver;
    ProductMapper.variantMapper = variantMapper;
  }

  public static ProductResponse toProductResponse(Product product) {
    ProductResponse response = new ProductResponse();

    final var discountPrice =
        PromotionCalculator.calculateDiscountedPrice(product.getPrice(), product.getPromotions());
    final var discountPercentage =
        PromotionCalculator.calculateDiscountPercentage(product.getPrice(), discountPrice);
    final var promotionBadge =
        PromotionCalculator.buildPromotionBadge(
            product.getPrice(), product.getPromotions(), discountPercentage);
    response.setId(product.getId());
    response.setName(product.getName());
    response.setCode(product.getCode());
    response.setUuid(product.getUuid());
    response.setPrice(product.getPrice());
    response.setDescription(product.getDescription());
    response.setIsFeature(product.getIsFeature());

    // Resolve each raw filename → full URL, sorted by sortOrder
    // This is the ONLY place URL resolution happens
    List<ProductImageResponse> imageDtos =
        product.getImages().stream()
            .sorted(Comparator.comparing(ProductImage::getSortOrder))
            .map(
                img ->
                    new ProductImageResponse(
                        img.getId(),
                        fileManagerService.getResourceUrl(
                            storageConfig.getProductPath(), img.getImagePath()),
                        img.getSortOrder()))
            .toList();
    response.setImages(imageDtos);

    // The first image in sorted order is the primary
    String primaryImageUrl = imageDtos.isEmpty() ? null : imageDtos.getFirst().getImagePath();

    final var auditUserMap =
        auditUserResolver.resolve(List.of(product.getCreatedBy(), product.getUpdatedBy()));

    response.setImages(imageDtos);
    response.setPrimaryImage(primaryImageUrl);
    response.setCategoryId(product.getCategory().getId());
    response.setCategoryName(product.getCategory().getName());
    response.setFavoritesCount(product.getFavoritesCount());
    response.setCreatedBy(auditUserMap.get(product.getCreatedBy()));
    response.setUpdatedBy(auditUserMap.get(product.getUpdatedBy()));
    response.setInStock(product.getInStock());
    response.setStockQuantity(product.getStockQuantity());
    response.setStockStatus(product.getStockStatus());
    response.setHasPromotion(product.getHasPromotion());
    response.setQuickAddAvailable(product.getQuickAddAvailable());
    response.setDiscountedPrice(discountPrice);
    response.setPromotionBadge(promotionBadge);
    response.setSpecs(product.getSpecTexts());
    response.setCode(product.getCode());
    response.setHasVariants(product.getHasVariants());
    response.setVariants(
        Boolean.TRUE.equals(product.getHasVariants()) && product.getVariants() != null
            ? product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                .map(variantMapper::toResponse) // ← needs variantMapper injected
                .toList()
            : List.of());
    response.setTotalStockQuantity(product.getTotalStockQuantity());
    response.setAggregatedStockStatus(product.getAggregatedStockStatus());
    // warranty
    WarrantyResponse warrantyResponse = new WarrantyResponse();
    warrantyResponse.setType(product.getWarrantyType());
    warrantyResponse.setDuration(product.getWarrantyDuration());
    warrantyResponse.setUnit(product.getWarrantyUnit());
    warrantyResponse.setDescription(product.getWarrantyDescription());

    // Build label
    warrantyResponse.setLabel(
        WarrantyUtil.buildLabel(
            product.getWarrantyType(), product.getWarrantyDuration(), product.getWarrantyUnit()));
    response.setWarranty(warrantyResponse);

    SimpleBrandResponse brandResponse = null;
    if (product.getBrand() != null) {
      brandResponse =
          SimpleBrandResponse.builder()
              .id(product.getBrand().getId())
              .name(product.getBrand().getName())
              .logo(
                  product.getBrand().getLogo() != null
                      ? fileManagerService.getResourceUrl(
                          storageConfig.getLogoPath(), product.getBrand().getLogo())
                      : null)
              .build();
    }
    response.setBrand(brandResponse);
    return response;
  }

  public static SimpleProductResponse toSimpleProductResponse(Product product) {
    SimpleProductResponse response = new SimpleProductResponse();

    response.setId(product.getId());
    response.setName(product.getName());
    response.setCode(product.getCode());
    response.setUuid(product.getUuid());
    response.setPrice(product.getPrice());

    List<ProductImageResponse> imageDtos =
        product.getImages().stream()
            .sorted(Comparator.comparing(ProductImage::getSortOrder))
            .map(
                img ->
                    new ProductImageResponse(
                        img.getId(),
                        fileManagerService.getResourceUrl(
                            storageConfig.getProductPath(), img.getImagePath()),
                        img.getSortOrder()))
            .toList();

    // First image in sorted order is the primary
    String primaryImageUrl = imageDtos.isEmpty() ? null : imageDtos.getFirst().getImagePath();

    response.setPrimaryImage(primaryImageUrl);
    response.setCategoryName(product.getCategory().getName());
    response.setStockStatus(product.getStockStatus());
    response.setCode(product.getCode());
    response.setHasVariants(product.getHasVariants());
    response.setVariants(
        Boolean.TRUE.equals(product.getHasVariants()) && product.getVariants() != null
            ? product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                .map(variantMapper::toResponse) // ← needs variantMapper injected
                .toList()
            : List.of());
    response.setTotalStockQuantity(product.getTotalStockQuantity());
    response.setAggregatedStockStatus(product.getAggregatedStockStatus());
    SimpleBrandResponse brandResponse = null;
    if (product.getBrand() != null) {
      brandResponse =
          SimpleBrandResponse.builder()
              .id(product.getBrand().getId())
              .name(product.getBrand().getName())
              .logo(
                  product.getBrand().getLogo() != null
                      ? fileManagerService.getResourceUrl(
                          storageConfig.getLogoPath(), product.getBrand().getLogo())
                      : null)
              .build();
    }
    response.setBrand(brandResponse);
    return response;
  }
}
