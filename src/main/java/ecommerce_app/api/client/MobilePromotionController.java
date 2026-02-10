package ecommerce_app.api.client;

import ecommerce_app.infrastructure.mapper.PromotionMapper;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionValidationRequest;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionValidationResponse;
import ecommerce_app.modules.promotion.service.MobilePromotionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "Mobile Promotion Controller", description = "Promotion APIs for mobile app")
public class MobilePromotionController {

  private final MobilePromotionService promotionService;
  private final PromotionMapper promotionMapper;

  /**
   * Get all active promotions for mobile app GET /api/mobile/v1/promotions/active?page=0&size=20
   */
  @GetMapping("/active")
  public ResponseEntity<BaseBodyResponse> getActivePromotions(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    return BaseBodyResponse.pageSuccess(
        promotionService.getActivePromotions(pageable), "Get active promotions successfully");
  }

  /** Get promotion detail by ID GET /api/mobile/v1/promotions/{id} */
  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> getPromotionById(@PathVariable Long id) {

    return BaseBodyResponse.success(
        promotionService.getPromotionById(id), "Get promotion by ID successfully");
  }

  /** Get promotion detail by code GET /api/mobile/v1/promotions/code/{code} */
  @GetMapping("/code/{code}")
  public ResponseEntity<BaseBodyResponse> getPromotionByCode(@PathVariable String code) {

    return BaseBodyResponse.success(
        promotionService.getPromotionByCode(code), "Get promotion by code successfully");
  }

  /** Validate promotion code for cart POST /api/mobile/v1/promotions/validate */
  @PostMapping("/validate")
  public ResponseEntity<BaseBodyResponse> validatePromotion(
      @RequestBody MobilePromotionValidationRequest request) {

    MobilePromotionValidationResponse validation =
        promotionService.validatePromotion(
            request.getCode(), request.getUserId(), request.getCartTotal());

    return BaseBodyResponse.success(validation, "Promotion validation successful");
  }

  /** Get upcoming promotions GET /api/mobile/v1/promotions/upcoming?size=10 */
  @GetMapping("/upcoming")
  public ResponseEntity<BaseBodyResponse> getUpcomingPromotions(
      @RequestParam(defaultValue = "10") int size) {

    return BaseBodyResponse.success(
        promotionService.getUpcomingPromotions(size), "Get upcoming promotions successfully");
  }

  /** Get featured/ the best promotions GET /api/mobile/v1/promotions/featured?size=5 */
  @GetMapping("/featured")
  public ResponseEntity<BaseBodyResponse> getFeaturedPromotions(
      @RequestParam(defaultValue = "5") int size) {

    return BaseBodyResponse.success(
        promotionService.getFeaturedPromotions(size), "Get featured promotions successfully");
  }
}
