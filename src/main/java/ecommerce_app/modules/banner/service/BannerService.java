package ecommerce_app.modules.banner.service;

import ecommerce_app.modules.banner.model.dto.BannerResponse;
import java.util.List;

public interface BannerService {
  List<BannerResponse> getActiveBanners(int limit);
}
