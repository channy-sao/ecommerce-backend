package ecommerce_app.modules.banner.service;

import ecommerce_app.infrastructure.mapper.BannerMapper;
import ecommerce_app.modules.banner.model.dto.BannerResponse;
import ecommerce_app.modules.banner.model.entity.Banner;
import ecommerce_app.modules.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileBannerService {

  private final BannerRepository bannerRepository;
  private final BannerMapper bannerMapper;

  /**
   * Get active banners for home carousel
   * Returns banners that are:
   * - Active
   * - Within date range (if specified)
   * - Position = HOME_CAROUSEL
   * - Ordered by display_order
   */
  @Transactional(readOnly = true)
  public List<BannerResponse> getHomeBanners(int limit) {
    log.info("Getting home banners with limit: {}", limit);
    
    List<Banner> banners = bannerRepository.findActiveHomeBanners(
        "HOME_CAROUSEL", 
        LocalDateTime.now());
    
    return banners.stream()
        .limit(limit)
        .map(bannerMapper::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get all active banners (for admin preview or other screens)
   */
  @Transactional(readOnly = true)
  public List<BannerResponse> getAllActiveBanners() {
    return bannerRepository.findAllActive()
        .stream()
        .map(bannerMapper::toResponse)
        .collect(Collectors.toList());
  }
}