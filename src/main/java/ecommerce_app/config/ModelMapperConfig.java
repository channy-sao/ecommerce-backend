package ecommerce_app.config;

import ecommerce_app.core.io.service.FileManagerService;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.property.StorageConfigProperty;
import ecommerce_app.dto.request.ProductRequest;
import ecommerce_app.entity.Product;
import ecommerce_app.dto.response.UserResponse;
import ecommerce_app.entity.User;
import ecommerce_app.util.AuditUserResolver;
import ecommerce_app.util.ProductMapper;
import org.modelmapper.Conditions;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class ModelMapperConfig {
  @Bean
  public ModelMapper modelMapper(
      FileManagerService fileManagerService,
      StorageConfigProperty storageConfigProperty,
      StorageConfig storageConfig,
      AuditUserResolver auditUserResolver) {
    ModelMapper modelMapper = new ModelMapper();
    ProductMapper.setProperties(
        modelMapper, fileManagerService, storageConfigProperty, storageConfig, auditUserResolver);

    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    // Converter to prepend full path
    Converter<String, String> avatarPathConverter =
        ctx ->
            ctx.getSource() == null
                ? null
                : fileManagerService.getResourceUrl(storageConfig.getAvatarPath(), ctx.getSource());

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
              mapper.skip(Product::setBrand);
            });

    // Add converter for PersistentSet to Set
    Converter<Set<?>, Set<?>> persistentSetConverter =
        context -> {
          if (context.getSource() == null) {
            return null;
          }
          return new HashSet<>(context.getSource());
        };

    modelMapper.addConverter(persistentSetConverter);

    // Configure to skip null values and avoid merging collections
    modelMapper
        .getConfiguration()
        .setMatchingStrategy(MatchingStrategies.STRICT)
        .setPropertyCondition(Conditions.isNotNull())
        .setSkipNullEnabled(true)
        .setCollectionsMergeEnabled(false) // ← IMPORTANT: Disable collection merging
        .setAmbiguityIgnored(true);

    return modelMapper;
  }
}
