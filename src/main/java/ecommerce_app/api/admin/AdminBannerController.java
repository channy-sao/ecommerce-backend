package ecommerce_app.api.admin;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.banner.model.dto.BannerRequest;
import ecommerce_app.modules.banner.model.dto.BannerResponse;
import ecommerce_app.modules.banner.service.BannerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/banners")
@RequiredArgsConstructor
@Tag(name = "Admin Banner Controller", description = "Banner management for admin")
public class AdminBannerController {
  private final BannerService bannerService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseBodyResponse<BannerResponse>> createBanner(
      @Valid @ModelAttribute BannerRequest request) {

    return BaseBodyResponse.success(
        bannerService.createBanner(request), "Banner created successfully");
  }

  @PutMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<BannerResponse>> updateBanner(
      @PathVariable("id") Long id, @Valid @ModelAttribute BannerRequest request) {
    return BaseBodyResponse.success(
        bannerService.updateBanner(id, request), "Banner updated successfully");
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<Void>> deleteBanner(@PathVariable("id") Long id) {
    bannerService.deleteBanner(id);
    return BaseBodyResponse.success("Banner deleted successfully");
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<BannerResponse>>> getBanners(
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "filter") String filter) {
    return BaseBodyResponse.pageSuccess(bannerService.getBanners(page, pageSize, filter), "Get all banners successfully");
  }

  @PatchMapping("/{id}/toggle-status")
  public ResponseEntity<BaseBodyResponse<Void>> toggleBannerStatus(@PathVariable("id") Long id) {
    bannerService.toggleBannerStatus(id);
    return BaseBodyResponse.success("Banner status toggled successfully");
  }
}
