package ecommerce_app.api.admin;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.banner.model.dto.BannerRequest;
import ecommerce_app.modules.banner.model.entity.Banner;
import ecommerce_app.modules.banner.repository.BannerRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/banners")
@RequiredArgsConstructor
@Tag(name = "Admin Banner Controller", description = "Banner management for admin")
public class AdminBannerController {

  private final BannerRepository bannerRepository;
  private final ModelMapper modelMapper;

  @PostMapping
  public ResponseEntity<BaseBodyResponse> createBanner(@Valid @ModelAttribute BannerRequest request) {

    Banner banner = modelMapper.map(request, Banner.class);
    Banner saved = bannerRepository.save(banner);

    return BaseBodyResponse.success(saved, "Banner created successfully");
  }

  @PutMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> updateBanner(
      @PathVariable Long id, @Valid @ModelAttribute BannerRequest request) {

    Banner banner =
        bannerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Banner", id));

    modelMapper.map(request, banner);
    Banner updated = bannerRepository.save(banner);

    return BaseBodyResponse.success(updated, "Banner updated successfully");
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> deleteBanner(@PathVariable Long id) {
    bannerRepository.deleteById(id);
    return BaseBodyResponse.success(null, "Banner deleted successfully");
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse> getAllBanners() {
    return BaseBodyResponse.success(bannerRepository.findAll(), "Get all banners successfully");
  }

  @PatchMapping("/{id}/toggle")
  public ResponseEntity<BaseBodyResponse> toggleBannerStatus(@PathVariable Long id) {
    Banner banner =
        bannerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Banner", id));

    banner.setIsActive(!banner.getIsActive());
    bannerRepository.save(banner);

    return BaseBodyResponse.success(null, "Banner status toggled successfully");
  }
}
