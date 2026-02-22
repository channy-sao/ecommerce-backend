package ecommerce_app.mapper;

import ecommerce_app.core.io.service.StaticResourceService;
import ecommerce_app.dto.response.BannerResponse;
import ecommerce_app.entity.Banner;
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
        .image(staticResourceService.getBannerImageUrl(banner.getImage()))
        .position(banner.getPosition())
        .linkUrl(banner.getLinkUrl())
        .linkType(banner.getLinkType())
        .active(banner.getIsActive())
        .linkId(banner.getLinkId())
        .displayOrder(banner.getDisplayOrder())
        .backgroundColor(banner.getBackgroundColor())
        .startDate(banner.getStartDate())
        .endDate(banner.getEndDate())
        .build();
  }
}
