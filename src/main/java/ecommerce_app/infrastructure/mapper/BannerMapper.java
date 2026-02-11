package ecommerce_app.infrastructure.mapper;

import ecommerce_app.modules.banner.model.dto.BannerResponse;
import ecommerce_app.modules.banner.model.entity.Banner;
import org.springframework.stereotype.Component;

@Component
public class BannerMapper {

  public BannerResponse toResponse(Banner banner) {
    return BannerResponse.builder()
        .id(banner.getId())
        .title(banner.getTitle())
        .imageUrl(banner.getImageUrl())
        .redirectUrl(banner.getRedirectUrl())
        .type(banner.getType())
        .build();
  }
}
