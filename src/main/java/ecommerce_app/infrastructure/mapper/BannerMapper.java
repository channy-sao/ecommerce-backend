package ecommerce_app.infrastructure.mapper;

import ecommerce_app.infrastructure.io.service.StaticResourceService;
import ecommerce_app.modules.banner.model.dto.BannerResponse;
import ecommerce_app.modules.banner.model.entity.Banner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BannerMapper {
  private final StaticResourceService staticResourceService;

  public BannerResponse toResponse(Banner banner) {
    if (banner == null) {
      return null;
    }

    return BannerResponse.builder()
        .id(banner.getId())
        .title(banner.getTitle())
        .description(banner.getDescription())
        .imageUrl(staticResourceService.getBannerImageUrl(banner.getImage()))
        .linkUrl(banner.getLinkUrl())
        .linkType(banner.getLinkType())
        .linkId(banner.getLinkId())
        .displayOrder(banner.getDisplayOrder())
        .backgroundColor(banner.getBackgroundColor())
        .startDate(banner.getStartDate())
        .endDate(banner.getEndDate())
        .build();
  }
}
