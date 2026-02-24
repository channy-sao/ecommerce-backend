package ecommerce_app.mapper;

import ecommerce_app.core.io.service.StaticResourceService;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.dto.response.BrandResponse;
import ecommerce_app.dto.response.SimpleBrandResponse;
import ecommerce_app.entity.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandMapper {

  private final StaticResourceService staticResourceService;

  public BrandResponse toResponse(Brand brand) {
    if (brand == null) return null;
    return BrandResponse.builder()
        .id(brand.getId())
        .name(brand.getName())
        .description(brand.getDescription())
        .logo(resolveUrl(brand.getLogo()))
        .isActive(brand.getIsActive())
        .displayOrder(brand.getDisplayOrder())
        .createdAt(brand.getCreatedAt() != null ? brand.getCreatedAt().toString() : null)
        .build();
  }

  public SimpleBrandResponse toSimpleResponse(Brand brand) {
    if (brand == null) return null;
    return SimpleBrandResponse.builder()
        .id(brand.getId())
        .name(brand.getName())
        .logo(resolveUrl(brand.getLogo()))
        .build();
  }

  private String resolveUrl(String path) {
    if (path == null || path.isBlank()) return null;
    return staticResourceService.getLogoUrl(path);
  }
}
