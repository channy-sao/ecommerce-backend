package ecommerce_app.infrastructure.io.service.impl;

import ecommerce_app.infrastructure.io.service.StorageConfig;
import ecommerce_app.infrastructure.property.StorageConfigProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageConfigImpl implements StorageConfig {
  private final StorageConfigProperty storageConfigProperty;

  @Override
  public String getBasePath() {
    return storageConfigProperty.getUpload();
  }

  @Override
  public String getAvatarPath() {
    return "%s/avatar".formatted(getBasePath());
  }

  @Override
  public String getProductPath() {
    return "%s/product".formatted(getBasePath());
  }

  @Override
  public String getCommonFilePath() {
    return "%s/files".formatted(getBasePath());
  }

  @Override
  public String getBannerPath() {
    return "%s/banner".formatted(getBasePath());
  }
}
