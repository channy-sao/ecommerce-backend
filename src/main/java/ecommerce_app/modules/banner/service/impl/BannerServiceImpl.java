package ecommerce_app.modules.banner.service.impl;

import ecommerce_app.infrastructure.mapper.BannerMapper;
import ecommerce_app.modules.banner.model.dto.BannerResponse;
import ecommerce_app.modules.banner.repository.BannerRepository;
import ecommerce_app.modules.banner.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

  private final BannerRepository bannerRepository;
  private final BannerMapper bannerMapper;

  @Override
  public List<BannerResponse> getActiveBanners(int limit) {

    LocalDateTime now = LocalDateTime.now();

    return bannerRepository
        .findByActiveTrueAndStartAtBeforeAndEndAtAfterOrderByDisplayOrderAsc(
            now, now, PageRequest.of(0, limit))
        .stream()
        .map(bannerMapper::toResponse)
        .toList();
  }
}
