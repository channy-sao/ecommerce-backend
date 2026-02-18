package ecommerce_app.util;

import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.io.service.StorageConfig;
import ecommerce_app.infrastructure.property.StorageConfigProperty;
import ecommerce_app.modules.product.model.dto.ProductImageDto;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.model.entity.ProductImage;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductMapper {

  private static ModelMapper modelMapper;
  private static FileManagerService fileManagerService;
  private static StorageConfigProperty storageConfigProperty;
  private static AuditUserResolver auditUserResolver;
  private static StorageConfig storageConfig;

  public static void setProperties(
      ModelMapper modelMapper,
      FileManagerService fileManagerService,
      StorageConfigProperty storageConfigProperty,
      StorageConfig storageConfig,
      AuditUserResolver auditUserResolver) {
    ProductMapper.modelMapper = modelMapper;
    ProductMapper.fileManagerService = fileManagerService;
    ProductMapper.storageConfigProperty = storageConfigProperty;
    ProductMapper.storageConfig = storageConfig;
    ProductMapper.auditUserResolver = auditUserResolver;
  }

  public static ProductResponse toProductResponse(Product product) {
    ProductResponse response = modelMapper.map(product, ProductResponse.class);

    // Resolve each raw filename → full URL, sorted by sortOrder
    // This is the ONLY place URL resolution happens
    List<ProductImageDto> imageDtos = product.getImages().stream()
            .sorted(Comparator.comparing(ProductImage::getSortOrder))
            .map(img -> new ProductImageDto(
                    img.getId(),
                    fileManagerService.getResourceUrl(storageConfig.getProductPath(), img.getImagePath()),
                    img.getSortOrder()
            ))
            .toList();
    response.setImages(imageDtos);

    // First image in sorted order is the primary
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
    return response;
  }
}
