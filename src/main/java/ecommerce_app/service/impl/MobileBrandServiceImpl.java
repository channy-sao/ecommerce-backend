package ecommerce_app.service.impl;

import ecommerce_app.core.io.service.FileManagerService;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.dto.request.BrandRequest;
import ecommerce_app.dto.response.BrandResponse;
import ecommerce_app.dto.response.SimpleBrandResponse;
import ecommerce_app.entity.Brand;
import ecommerce_app.exception.ConflictException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.BrandMapper;
import ecommerce_app.repository.BrandRepository;
import ecommerce_app.service.BrandService;
import java.util.List;

import ecommerce_app.service.MobileBrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MobileBrandServiceImpl implements MobileBrandService {

  private final BrandRepository brandRepository;
  private final BrandMapper brandMapper;

  @Transactional(readOnly = true)
  public List<BrandResponse> getAllBrands() {
    log.info("Getting all brands");
    return brandRepository.findAll().stream().map(brandMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<SimpleBrandResponse> getActiveBrands() {
    log.info("Getting active brands");
    return brandRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc().stream()
        .map(brandMapper::toSimpleResponse)
        .toList();
  }


  @Transactional(readOnly = true)
  @Override
  public Page<SimpleBrandResponse> searchBrands(String search, int page, int size) {
    Page<Brand> brandPage =
        brandRepository.findActiveBrandsByNameContaining(
            search, PageRequest.of(page - 1, size)); // start from 0
    return brandPage.map(brandMapper::toSimpleResponse);
  }
}
