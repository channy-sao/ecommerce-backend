package ecommerce_app.infrastructure.mapper;

import ecommerce_app.modules.banner.model.dto.BannerResponse;
import ecommerce_app.modules.banner.model.entity.Banner;
import org.springframework.stereotype.Component;

@Component
public class BannerMapper {

  public BannerResponse toResponse(Banner banner) {
    if (banner == null) {
      return null;
    }

    return BannerResponse.builder()
        .id(banner.getId())
        .title(banner.getTitle())
        .description(banner.getDescription())
        .imageUrl(banner.getImageUrl())
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