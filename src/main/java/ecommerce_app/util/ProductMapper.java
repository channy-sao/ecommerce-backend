package ecommerce_app.util;

import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.property.StorageConfigProperty;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import ecommerce_app.modules.product.model.entity.Product;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductMapper {
  private static ModelMapper modelMapper;
  private static FileManagerService fileManagerService;
  private static StorageConfigProperty storageConfigProperty;

  public static void setProperties(
      ModelMapper modelMapper,
      FileManagerService fileManagerService,
      StorageConfigProperty storageConfigProperty) {
    ProductMapper.modelMapper = modelMapper;
    ProductMapper.fileManagerService = fileManagerService;
    ProductMapper.storageConfigProperty = storageConfigProperty;
  }

  public static ProductResponse toProductResponse(Product product) {
    ProductResponse response = modelMapper.map(product, ProductResponse.class);
    final var image =
        product.getImage() == null || product.getImage().isEmpty()
            ? null
            : fileManagerService.getResourceUrl(
                storageConfigProperty.getProduct(), product.getImage());
    response.setImage(image);
    response.setCategoryId(product.getCategory().getId());
    response.setCategoryName(product.getCategory().getName());
    return response;
  }
}
