package ecommerce_app.modules.banner.service.impl;

import ecommerce_app.infrastructure.exception.BadRequestException;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.io.service.StaticResourceService;
import ecommerce_app.infrastructure.io.service.StorageConfig;
import ecommerce_app.infrastructure.mapper.BannerMapper;
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
  private final BannerMapper bannerMapper;

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
      log.info("Created new banner: id={}", saved.getId());
      return bannerMapper.toResponse(saved);
    } catch (DataIntegrityViolationException ex) {
      log.error(ex.getMessage(), ex);
      throw new BadRequestException(ex.getMessage());
    }
  }

  @Transactional
  @Override
  public BannerResponse updateBanner(Long bannerId, BannerRequest bannerRequest) {
    log.info("Updating banner request: id={}", bannerId);
    try {
      final var existingBanner =
          bannerRepository
              .findById(bannerId)
              .orElseThrow(() -> new ResourceNotFoundException("Banner", bannerId));
        this.prepareBannerEntity(bannerRequest, existingBanner);
        if (bannerRequest.getImage() != null) {
            String imagePath =
                fileManagerService.saveFile(bannerRequest.getImage(), storageConfig.getBannerPath());
            if (imagePath != null) {
                existingBanner.setImage(imagePath);
            }
        }
      Banner updated = bannerRepository.save(existingBanner);
      return bannerMapper.toResponse(updated);
    }
    catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new BadRequestException(ex.getMessage());
    }
  }

  @Transactional
  @Override
  public void deleteBanner(Long bannerId) {
    log.info("Deleting banner request");
    try {
      bannerRepository.deleteById(bannerId);
      log.info("Deleted banner: id={}", bannerId);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new BadRequestException(ex.getMessage());
    }
  }

  @Transactional
  @Override
  public BannerResponse getBannerById(Long bannerId) {
    log.info("Getting banner by id: {}", bannerId);
    var banner =
        bannerRepository
            .findById(bannerId)
            .orElseThrow(() -> new ResourceNotFoundException("Banner", bannerId));
    return bannerMapper.toResponse(banner);
  }

  @Transactional
  @Override
  public void toggleBannerStatus(Long bannerId) {
    log.info("Toggling banner status: id={}", bannerId);
    final var banner =
        bannerRepository
            .findById(bannerId)
            .orElseThrow(() -> new ResourceNotFoundException("Banner", bannerId));
    banner.setIsActive(!banner.getIsActive());
    bannerRepository.save(banner);
    log.info("Toggled banner status: id={}, newStatus={}", bannerId, banner.getIsActive());
  }

  @Transactional(readOnly = true)
  @Override
  public List<BannerResponse> getAllBanners() {
    log.info("Getting all banners");
    return bannerRepository.findAll().stream().map(bannerMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  @Override
  public Page<BannerResponse> getBanners(int page, int pageSize, String filter, String activeFilter) {
    log.info("Getting banners with pagination: page={}, pageSize={}", page, pageSize);
    // start from 0
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    Page<Banner> bannerPage = null;
    if(activeFilter!= null && !activeFilter.equalsIgnoreCase("all")) {
      log.info("Filtering banners by active status: {}", activeFilter);
    boolean isActive = activeFilter.equalsIgnoreCase("active");
     bannerPage = bannerRepository.findAllByTitleLike(filter, isActive, pageable);
    }
    else {
      bannerPage = bannerRepository.findAllByTitleLike(filter, pageable);
    }
    return bannerPage.map(bannerMapper::toResponse);
  }

  private void prepareBannerEntity(final BannerRequest bannerRequest, final Banner banner) {
    banner.setTitle(bannerRequest.getTitle());
    banner.setDescription(bannerRequest.getDescription());
    banner.setIsActive(bannerRequest.getIsActive());
    banner.setBackgroundColor(bannerRequest.getBackgroundColor());
    banner.setDisplayOrder(bannerRequest.getDisplayOrder());
    banner.setEndDate(bannerRequest.getEndDate());
    banner.setStartDate(bannerRequest.getStartDate());
    banner.setLinkId(bannerRequest.getLinkId());
    banner.setLinkType(bannerRequest.getLinkType());
    banner.setPosition(bannerRequest.getPosition());
  }
}
