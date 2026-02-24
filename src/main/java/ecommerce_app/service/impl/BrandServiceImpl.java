package ecommerce_app.service.impl;

import ecommerce_app.core.io.service.FileManagerService;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.dto.request.BrandRequest;
import ecommerce_app.dto.response.BrandResponse;
import ecommerce_app.entity.Brand;
import ecommerce_app.exception.ConflictException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.BrandMapper;
import ecommerce_app.repository.BrandRepository;
import ecommerce_app.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

  private final BrandRepository brandRepository;
  private final FileManagerService fileManagerService;
  private final StorageConfig storageConfig;
  private final BrandMapper brandMapper;

  @Transactional(readOnly = true)
  public List<BrandResponse> getAllBrands() {
    log.info("Getting all brands");
    return brandRepository.findAll().stream().map(brandMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<BrandResponse> getActiveBrands() {
    log.info("Getting active brands");
    return brandRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc().stream()
        .map(brandMapper::toResponse)
        .toList();
  }

  @Transactional(rollbackFor = Exception.class)
  public BrandResponse createBrand(BrandRequest request) {
    log.info("Creating new brand");
    if (brandRepository.existsByNameIgnoreCase(request.getName())) {
      throw new ConflictException("Brand name already exists");
    }

    String logoPath = null;
    if (request.getLogo() != null && !request.getLogo().isEmpty()) {
      logoPath = fileManagerService.saveFile(request.getLogo(), storageConfig.getLogoPath());
    }

    Brand brand =
        Brand.builder()
            .name(request.getName())
            .description(request.getDescription())
            .logo(logoPath)
            .isActive(request.getIsActive())
            .displayOrder(request.getDisplayOrder())
            .build();

    return brandMapper.toResponse(brandRepository.save(brand));
  }

  @Transactional(rollbackFor = Exception.class)
  public BrandResponse updateBrand(Long id, BrandRequest request) {
    log.info("Updating brand");
    Brand brand =
        brandRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand", id));

    // Check name conflict (exclude self)
    brandRepository
        .findByNameIgnoreCase(request.getName())
        .filter(b -> !b.getId().equals(id))
        .ifPresent(
            b -> {
              throw new ConflictException("Brand name already exists");
            });

    if (request.getLogo() != null && !request.getLogo().isEmpty()) {
      // Delete old logo
      if (brand.getLogo() != null) {
        fileManagerService.deleteFile(storageConfig.getLogoPath(), brand.getLogo());
      }
      brand.setLogo(fileManagerService.saveFile(request.getLogo(), storageConfig.getLogoPath()));
    }

    brand.setName(request.getName());
    brand.setDescription(request.getDescription());
    brand.setIsActive(request.getIsActive());
    brand.setDisplayOrder(request.getDisplayOrder());

    return brandMapper.toResponse(brandRepository.save(brand));
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteBrand(Long id) {
    log.info("Deleting brand");
    Brand brand =
        brandRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
    if (brand.getLogo() != null) {
      fileManagerService.deleteFile(storageConfig.getLogoPath(), brand.getLogo());
    }
    brandRepository.delete(brand);
  }

  @Transactional(readOnly = true)
  @Override
  public BrandResponse getById(Long id) {
    log.info("Getting brand");
    final var brand =
        brandRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand", id));
    return brandMapper.toResponse(brand);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void toggleStatus(Long id) {
    log.info("Toggle status");
    final var brand =
        brandRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand", id));
    brand.setIsActive(!brand.getIsActive());
    brandRepository.save(brand);
  }
}
