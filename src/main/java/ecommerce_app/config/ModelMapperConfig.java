package ecommerce_app.config;

import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.property.StorageConfigProperty;
import ecommerce_app.modules.product.model.dto.ProductRequest;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.user.model.dto.UserResponse;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.util.AuditUserResolver;
import ecommerce_app.util.ProductMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
  @Bean
  public ModelMapper modelMapper(
      FileManagerService fileManagerService,
      StorageConfigProperty storageConfigProperty,
      AuditUserResolver auditUserResolver) {
    ModelMapper modelMapper = new ModelMapper();
    ProductMapper.setProperties(
        modelMapper, fileManagerService, storageConfigProperty, auditUserResolver);

    // Converter to prepend full path
    Converter<String, String> avatarPathConverter =
        ctx ->
            ctx.getSource() == null
                ? null
                : fileManagerService.getResourceUrl(
                    storageConfigProperty.getAvatar(), ctx.getSource());

    // Apply the converter
    modelMapper
        .typeMap(User.class, UserResponse.class)
        .addMappings(
            mapper ->
                mapper.using(avatarPathConverter).map(User::getAvatar, UserResponse::setAvatar));

    modelMapper
        .typeMap(ProductRequest.class, Product.class)
        .addMappings(
            mapper -> {
              mapper.skip(Product::setId);
              mapper.skip(Product::setCategory);
            });

    return modelMapper;
  }
}
