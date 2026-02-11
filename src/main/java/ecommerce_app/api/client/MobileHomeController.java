package ecommerce_app.api.client;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.home.service.MobileHomeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/v1/home")
@RequiredArgsConstructor
@Tag(name = "Mobile Home Controller", description = "Home screen APIs for mobile app")
public class MobileHomeController {

  private final MobileHomeService homeService;

  /**
   * Get all home screen data in one request GET /api/client/v1/home
   *
   * <p>Returns: - Featured promotions (banners/carousel) - Featured products - New arrivals -
   * Popular products - Categories (optional)
   */
  @GetMapping
  public ResponseEntity<BaseBodyResponse> getHomeScreenData(
      @RequestParam(defaultValue = "5") int bannersSize,
      @RequestParam(defaultValue = "5") int featuredPromotionsSize,
      @RequestParam(defaultValue = "10") int featuredProductsSize,
      @RequestParam(defaultValue = "10") int newArrivalsSize,
      @RequestParam(defaultValue = "10") int popularProductsSize) {

    return BaseBodyResponse.success(
        homeService.getHomeScreenData(
            bannersSize,
            featuredPromotionsSize,
            featuredProductsSize,
            newArrivalsSize,
            popularProductsSize),
        "Get home screen data successfully");
  }
}
