package ecommerce_app.infrastructure.io.service;

public interface StorageConfig {
    String getBasePath();
    String getAvatarPath();
    String getProductPath();
    String getCommonFilePath();
    String getBannerPath();
}
