package ecommerce_app.api.admin;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.banner.model.dto.BannerRequest;
import ecommerce_app.modules.banner.service.BannerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/banners")
@RequiredArgsConstructor
@Tag(name = "Admin Banner Controller", description = "Banner management for admin")
public class AdminBannerController {
  private final BannerService bannerService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseBodyResponse> createBanner(
      @Valid @ModelAttribute BannerRequest request) {

    return BaseBodyResponse.success(
        bannerService.createBanner(request), "Banner created successfully");
  }

  @PutMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> updateBanner(
      @PathVariable Long id, @Valid @ModelAttribute BannerRequest request) {
    return BaseBodyResponse.success(
        bannerService.updateBanner(id, request), "Banner updated successfully");
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> deleteBanner(@PathVariable Long id) {
    bannerService.deleteBanner(id);
    return BaseBodyResponse.success(null, "Banner deleted successfully");
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse> getAllBanners() {
    return BaseBodyResponse.success(bannerService.getAllBanners(), "Get all banners successfully");
  }

  @PatchMapping("/{id}/toggle")
  public ResponseEntity<BaseBodyResponse> toggleBannerStatus(@PathVariable Long id) {
    bannerService.toggleBannerStatus(id);
    return BaseBodyResponse.success(null, "Banner status toggled successfully");
  }
}
