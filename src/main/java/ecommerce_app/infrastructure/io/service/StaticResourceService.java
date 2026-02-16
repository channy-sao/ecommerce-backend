package ecommerce_app.infrastructure.io.service;

public interface StaticResourceService {
    String getUserAvatarUrl(final String fileName);
    String getProductImageUrl(final String fileName);
    String getBannerImageUrl(final String fileName);
    String getCommonFileUrl(final String fileName);
}
