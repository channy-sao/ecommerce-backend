package ecommerce_app.modules.banner.service;

import ecommerce_app.modules.banner.model.dto.BannerRequest;
import ecommerce_app.modules.banner.model.dto.BannerResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BannerService {
  BannerResponse createBanner(BannerRequest bannerRequest);

  BannerResponse updateBanner(Long bannerId, BannerRequest bannerRequest);

  void deleteBanner(Long bannerId);

  BannerResponse getBannerById(Long bannerId);

  void toggleBannerStatus(Long bannerId);

  List<BannerResponse> getAllBanners();

  Page<BannerResponse> getBanners(int page, int pageSize);
}
