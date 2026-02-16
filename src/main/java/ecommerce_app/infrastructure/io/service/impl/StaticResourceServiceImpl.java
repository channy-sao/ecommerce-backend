package ecommerce_app.infrastructure.io.service.impl;

import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.io.service.StaticResourceService;
import ecommerce_app.infrastructure.io.service.StorageConfig;
import ecommerce_app.infrastructure.property.StorageConfigProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaticResourceServiceImpl implements StaticResourceService {
  private final FileManagerService fileManagerService;
  private final StorageConfigProperty storageConfigProperty;
  private final StorageConfig storageConfig;

  @Override
  public String getUserAvatarUrl(final String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return null;
    }
    return fileManagerService.getResourceUrl(storageConfig.getAvatarPath(), fileName);
  }

  @Override
  public String getProductImageUrl(final String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return null;
    }
    return fileManagerService.getResourceUrl(storageConfig.getProductPath(), fileName);
  }

  @Override
  public String getBannerImageUrl(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return null;
    }
    return fileManagerService.getResourceUrl(storageConfig.getBannerPath(), fileName);
  }

  @Override
  public String getCommonFileUrl(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return null;
    }
    return fileManagerService.getResourceUrl(storageConfig.getCommonFilePath(), fileName);
  }
}
