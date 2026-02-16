package ecommerce_app.modules.banner.service.impl;

import ecommerce_app.infrastructure.exception.BadRequestException;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.io.service.StaticResourceService;
import ecommerce_app.infrastructure.io.service.StorageConfig;
import ecommerce_app.infrastructure.property.StorageConfigProperty;
import ecommerce_app.modules.banner.model.dto.BannerRequest;
import ecommerce_app.modules.banner.model.dto.BannerResponse;
import ecommerce_app.modules.banner.model.entity.Banner;
import ecommerce_app.modules.banner.repository.BannerRepository;
import ecommerce_app.modules.banner.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerServiceImpl implements BannerService {
  private final BannerRepository bannerRepository;
  private final ModelMapper modelMapper;
  private final FileManagerService fileManagerService;
  private final StorageConfig storageConfig;

  @Transactional
  @Override
  public BannerResponse createBanner(BannerRequest bannerRequest) {
    log.info("Creating new banner request");
    try {
      Banner banner = modelMapper.map(bannerRequest, Banner.class);
      if (bannerRequest.getImage() != null) {
        String imagePath =
            fileManagerService.saveFile(bannerRequest.getImage(), storageConfig.getBannerPath());
        if (imagePath != null) {
          banner.setImage(imagePath);
        }
      }
      var saved = bannerRepository.save(banner);
      return modelMapper.map(saved, BannerResponse.class);
    } catch (DataIntegrityViolationException ex) {
      log.error(ex.getMessage(), ex);
      throw new BadRequestException(ex.getMessage());
    }
  }

  @Transactional
  @Override
  public BannerResponse updateBanner(Long bannerId, BannerRequest bannerRequest) {
    return null;
  }

  @Transactional
  @Override
  public void deleteBanner(Long bannerId) {
    log.info("Deleting banner request");
    try {
      bannerRepository.deleteById(bannerId);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new BadRequestException(ex.getMessage());
    }
  }

  @Transactional
  @Override
  public BannerResponse getBannerById(Long bannerId) {
    var banner =
        bannerRepository
            .findById(bannerId)
            .orElseThrow(() -> new ResourceNotFoundException("Banner", bannerId));
    return modelMapper.map(banner, BannerResponse.class);
  }

  @Transactional
  @Override
  public void toggleBannerStatus(Long bannerId) {
    final var banner =
        bannerRepository
            .findById(bannerId)
            .orElseThrow(() -> new ResourceNotFoundException("Banner", bannerId));
    banner.setIsActive(!banner.getIsActive());
  }

  @Transactional(readOnly = true)
  @Override
  public List<BannerResponse> getAllBanners() {
    return bannerRepository.findAll().stream()
        .map(b -> modelMapper.map(b, BannerResponse.class))
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public Page<BannerResponse> getBanners(int page, int pageSize) {
    // start from 0
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    final var bannerPage = bannerRepository.findAll(pageable);
    return bannerPage.map(b -> modelMapper.map(b, BannerResponse.class));
  }
}
