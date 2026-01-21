package ecommerce_app.api.admin;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.promotion.model.dto.PromotionRequest;
import ecommerce_app.modules.promotion.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

  @PostMapping
  @Operation(summary = "Create a new promotion")
  public ResponseEntity<BaseBodyResponse> createPromotion(
      @Valid @RequestBody PromotionRequest request) {
    return BaseBodyResponse.success(
        promotionService.createPromotion(request), "Promotion created Successfully");
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing promotion")
  public ResponseEntity<BaseBodyResponse> updatePromotion(
      @PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
    return BaseBodyResponse.success(
        promotionService.updatePromotion(id, request), "Promotion updated successfully");
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get promotion by ID")
  public ResponseEntity<BaseBodyResponse> getPromotion(@PathVariable Long id) {
    return BaseBodyResponse.success(promotionService.getPromotion(id), "Get promotion by ID");
  }

  @GetMapping
  @Operation(summary = "Get all promotions")
  public ResponseEntity<BaseBodyResponse> getAllPromotions() {
    return BaseBodyResponse.success(promotionService.getAllPromotions(), "Get all promotions");
  }

  @GetMapping("/active")
  @Operation(summary = "Get all active promotions")
  public ResponseEntity<BaseBodyResponse> getActivePromotions() {
    return BaseBodyResponse.success(
        promotionService.getActivePromotions(), "Get all active promotions Successfully");
  }

  @GetMapping("/product/{productId}")
  @Operation(summary = "Get promotions for a specific product")
  public ResponseEntity<BaseBodyResponse> getPromotionsByProduct(@PathVariable Long productId) {
    return BaseBodyResponse.success(
        promotionService.getPromotionsByProduct(productId),
        "Get promotions for a specific product");
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a promotion")
  public ResponseEntity<BaseBodyResponse> deletePromotion(@PathVariable Long id) {
    promotionService.deletePromotion(id);
    return BaseBodyResponse.success(null, "Deleted promotion");
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Toggle promotion status")
  public ResponseEntity<BaseBodyResponse> togglePromotionStatus(
      @PathVariable Long id, @RequestParam boolean active) {
    return BaseBodyResponse.success(
        promotionService.togglePromotionStatus(id, active), "Toggle promotion status successfully");
  }

  @PostMapping("/validate")
  @Operation(summary = "Validate and apply promotion to product")
  public ResponseEntity<BaseBodyResponse> validatePromotion(
      @RequestParam Long productId, @RequestParam String promotionCode) {
    return BaseBodyResponse.success(
        promotionService.validateAndApplyPromotion(productId, promotionCode),
        "Promotion validation successfully");
  }
}
