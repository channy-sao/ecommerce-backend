package ecommerce_app.api.client;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.banner.model.dto.BannerResponse;
import ecommerce_app.modules.banner.service.BannerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/v1/banners")
@RequiredArgsConstructor
@Tag(name = "Mobile Banner Controller", description = "Mobile banner controller for carousel")
public class MobileBannerController {

  private final BannerService bannerService;

  @GetMapping
  public ResponseEntity<BaseBodyResponse> getHomeBanners(
      @RequestParam(defaultValue = "5") int limit) {
    return BaseBodyResponse.success(bannerService.getActiveBanners(limit), "Successfully");
  }
}
