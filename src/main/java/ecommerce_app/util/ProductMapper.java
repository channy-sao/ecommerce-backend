package ecommerce_app.util;

import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.property.StorageConfigProperty;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import ecommerce_app.modules.product.model.entity.Product;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductMapper {
  private static ModelMapper modelMapper;
  private static FileManagerService fileManagerService;
  private static StorageConfigProperty storageConfigProperty;
  private static AuditUserResolver auditUserResolver;

  public static void setProperties(
      ModelMapper modelMapper,
      FileManagerService fileManagerService,
      StorageConfigProperty storageConfigProperty,
      AuditUserResolver auditUserResolver) {
    ProductMapper.modelMapper = modelMapper;
    ProductMapper.fileManagerService = fileManagerService;
    ProductMapper.storageConfigProperty = storageConfigProperty;
    ProductMapper.auditUserResolver = auditUserResolver;
  }

  public static ProductResponse toProductResponse(Product product) {
    ProductResponse response = modelMapper.map(product, ProductResponse.class);
    final var image =
        product.getImage() == null || product.getImage().isEmpty()
            ? null
            : fileManagerService.getResourceUrl(
                storageConfigProperty.getProduct(), product.getImage());
    final var auditUserMap =
        auditUserResolver.resolve(List.of(product.getCreatedBy(), product.getUpdatedBy()));
    response.setImage(image);
    response.setCategoryId(product.getCategory().getId());
    response.setCategoryName(product.getCategory().getName());
    response.setFavoritesCount(product.getFavoritesCount());
    response.setCreatedBy(auditUserMap.get(product.getCreatedBy()));
    response.setUpdatedBy(auditUserMap.get(product.getUpdatedBy()));
    return response;
  }
}
