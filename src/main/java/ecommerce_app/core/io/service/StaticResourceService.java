package ecommerce_app.core.io.service;

public interface StaticResourceService {
    String getUserAvatarUrl(final String fileName);
    String getProductImageUrl(final String fileName);
    String getBannerImageUrl(final String fileName);
    String getCommonFileUrl(final String fileName);
    String getLogoUrl(final String fileName);
}
