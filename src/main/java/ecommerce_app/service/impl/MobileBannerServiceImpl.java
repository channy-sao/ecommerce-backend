package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.BannerPosition;
import ecommerce_app.mapper.BannerMapper;
import ecommerce_app.dto.response.BannerResponse;
import ecommerce_app.entity.Banner;
import ecommerce_app.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileBannerServiceImpl {

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
    
    List<Banner> banners = bannerRepository.findActiveByPosition(
        BannerPosition.HOME_CAROUSEL,
        LocalDateTime.now());
    
    return banners.stream()
        .limit(limit)
        .map(bannerMapper::toResponse)
        .toList();
  }

  /**
   * Get active banners for home carousel
   * Returns banners that are:
   * - Active
   * - Within date range (if specified)
   * - Position = MIDDLE_SECTION
   * - Ordered by display_order
   */
  @Transactional(readOnly = true)
  public List<BannerResponse> getMiddleBanner(int limit) {
    log.info("Getting middle banners with limit: {}", limit);

    List<Banner> banners = bannerRepository.findActiveByPosition(
            BannerPosition.MIDDLE_SECTION,
            LocalDateTime.now());

    return banners.stream()
            .limit(limit)
            .map(bannerMapper::toResponse)
            .toList();
  }

  /**
   * Get all active banners (for admin preview or other screens)
   */
  @Transactional(readOnly = true)
  public List<BannerResponse> getAllActiveBanners() {
    return bannerRepository.findAllActive()
        .stream()
        .map(bannerMapper::toResponse)
        .toList();
  }
}