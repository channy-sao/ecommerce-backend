package ecommerce_app.controller.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.request.PromotionRequest;
import ecommerce_app.dto.response.PromotionResponse;
import ecommerce_app.entity.Promotion;
import ecommerce_app.service.PromotionService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/promotions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Promotion Management", description = "For admin manage promotion")
public class PromotionController {
  private final PromotionService promotionService;
  private final MessageSourceService messageSourceService;

  @PreAuthorize(
      "hasAnyAuthority('PROMOTION_CREATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN','MANAGER', 'SUPERVISOR')")
  @PostMapping
  @Operation(summary = "Create a new promotion")
  public ResponseEntity<BaseBodyResponse<PromotionResponse>> createPromotion(
      @Valid @RequestBody PromotionRequest request) {
    return BaseBodyResponse.success(
        promotionService.createPromotion(request),
        messageSourceService.getMessage(MessageKeyConstant.PROMOTION_MESSAGE_ADD_SUCCESS));
  }

  @PreAuthorize(
      "hasAnyAuthority('PROMOTION_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN','MANAGER', 'SUPERVISOR')")
  @PutMapping("/{id}")
  @Operation(summary = "Update an existing promotion")
  public ResponseEntity<BaseBodyResponse<PromotionResponse>> updatePromotion(
      @PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
    return BaseBodyResponse.success(
        promotionService.updatePromotion(id, request),
        messageSourceService.getMessage(MessageKeyConstant.PROMOTION_MESSAGE_UPDATE_SUCCESS));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get promotion by ID")
  public ResponseEntity<BaseBodyResponse<PromotionResponse>> getPromotion(@PathVariable Long id) {
    return BaseBodyResponse.success(
        promotionService.getPromotion(id),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @GetMapping
  @Operation(summary = "Get all promotions")
  public ResponseEntity<BaseBodyResponse<List<PromotionResponse>>> getAllPromotions() {
    return BaseBodyResponse.success(
        promotionService.getAllPromotions(),
        messageSourceService.getMessage(MessageKeyConstant.PROMOTION_TITLE_LIST));
  }

  @GetMapping("/filter")
  @Operation(summary = "Get all promotions as page")
  public ResponseEntity<BaseBodyResponse<List<PromotionResponse>>> getPromotionPage(
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
      @RequestParam(value = "query", required = false) String query,
      @RequestParam(value = "active", required = false) Boolean active,
      @RequestParam(value = "discountType", required = false) String discountType) {
    return BaseBodyResponse.pageSuccess(
        promotionService.getPromotionsByPage(query, active, discountType, page, pageSize),
        messageSourceService.getMessage(MessageKeyConstant.PROMOTION_TITLE_LIST));
  }

  @GetMapping("/active")
  @Operation(summary = "Get all active promotions")
  public ResponseEntity<BaseBodyResponse<List<PromotionResponse>>> getActivePromotions() {
    return BaseBodyResponse.success(
        promotionService.getActivePromotions(),
        messageSourceService.getMessage(MessageKeyConstant.PROMOTION_TITLE_LIST));
  }

  @GetMapping("/product/{productId}")
  @Operation(summary = "Get promotions for a specific product")
  public ResponseEntity<BaseBodyResponse<List<PromotionResponse>>> getPromotionsByProduct(
      @PathVariable Long productId) {
    return BaseBodyResponse.success(
        promotionService.getPromotionsByProduct(productId),
        messageSourceService.getMessage(MessageKeyConstant.PROMOTION_TITLE_LIST));
  }

  @PreAuthorize(
      "hasAnyAuthority('PROMOTION_DELETE') or hasAnyRole('ADMIN', 'SUPER_ADMIN','MANAGER', 'SUPERVISOR')")
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a promotion")
  public ResponseEntity<BaseBodyResponse<Void>> deletePromotion(@PathVariable Long id) {
    promotionService.deletePromotion(id);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.PROMOTION_MESSAGE_DELETE_SUCCESS));
  }

  @PreAuthorize(
      "hasAnyAuthority('PROMOTION_CREATE','PROMOTION_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN','MANAGER', 'SUPERVISOR')")
  @PatchMapping("/{id}/status")
  @Operation(summary = "Toggle promotion status")
  public ResponseEntity<BaseBodyResponse<PromotionResponse>> togglePromotionStatus(
      @PathVariable Long id, @RequestParam boolean active) {
    return BaseBodyResponse.success(
        promotionService.togglePromotionStatus(id, active),
        messageSourceService.getMessage(MessageKeyConstant.PROMOTION_MESSAGE_UPDATE_SUCCESS));
  }

  @PostMapping("/validate")
  @Operation(summary = "Validate and apply promotion to product")
  public ResponseEntity<BaseBodyResponse<Promotion>> validatePromotion(
      @RequestParam Long productId, @RequestParam String promotionCode) {
    return BaseBodyResponse.success(
        promotionService.validateAndApplyPromotion(productId, promotionCode),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
