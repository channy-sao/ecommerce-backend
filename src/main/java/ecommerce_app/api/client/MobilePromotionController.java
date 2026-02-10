package ecommerce_app.api.client;

import ecommerce_app.infrastructure.mapper.PromotionMapper;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionListResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionResponse;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionValidationRequest;
import ecommerce_app.modules.promotion.model.dto.MobilePromotionValidationResponse;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import ecommerce_app.modules.promotion.service.impl.MobilePromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mobile/v1/promotions")
@RequiredArgsConstructor
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
    Page<Promotion> promotionPage = promotionService.getActivePromotions(pageable);

    Page<MobilePromotionListResponse> responses =
        promotionPage.map(promotionMapper::toListResponse);

    return BaseBodyResponse.pageSuccess(responses, "Get active promotions successfully");
  }

  /** Get promotion detail by ID GET /api/mobile/v1/promotions/{id} */
  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> getPromotionById(@PathVariable Long id) {
    Promotion promotion = promotionService.getPromotionById(id);
    MobilePromotionResponse response = promotionMapper.toDetailResponse(promotion);
    return BaseBodyResponse.success(response, "Get promotion by ID successfully");
  }

  /** Get promotion detail by code GET /api/mobile/v1/promotions/code/{code} */
  @GetMapping("/code/{code}")
  public ResponseEntity<BaseBodyResponse> getPromotionByCode(@PathVariable String code) {
    Promotion promotion = promotionService.getPromotionByCode(code);
    MobilePromotionResponse response = promotionMapper.toDetailResponse(promotion);
    return BaseBodyResponse.success(response, "Get promotion by code successfully");
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

    List<Promotion> promotions = promotionService.getUpcomingPromotions(size);
    List<MobilePromotionListResponse> response =
        promotions.stream().map(promotionMapper::toListResponse).toList();

    return BaseBodyResponse.success(response, "Get upcoming promotions successfully");
  }

  /** Get featured/ the best promotions GET /api/mobile/v1/promotions/featured?size=5 */
  @GetMapping("/featured")
  public ResponseEntity<BaseBodyResponse> getFeaturedPromotions(
      @RequestParam(defaultValue = "5") int size) {

    List<Promotion> promotions = promotionService.getFeaturedPromotions(size);
    List<MobilePromotionListResponse> response =
        promotions.stream().map(promotionMapper::toListResponse).collect(Collectors.toList());

    return BaseBodyResponse.success(response, "Get featured promotions successfully");
  }
}
