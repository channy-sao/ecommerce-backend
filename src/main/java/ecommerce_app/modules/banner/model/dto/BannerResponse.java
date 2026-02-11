package ecommerce_app.modules.banner.model.dto;

import ecommerce_app.constant.enums.BannerType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BannerResponse {

  private Long id;
  private String title;
  private String imageUrl;
  private String redirectUrl;
  private BannerType type;
}
