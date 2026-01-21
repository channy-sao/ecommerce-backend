package ecommerce_app.modules.promotion.service;

import ecommerce_app.modules.promotion.model.dto.PromotionRequest;
import ecommerce_app.modules.promotion.model.dto.PromotionResponse;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import java.util.List;

public interface PromotionService {
  PromotionResponse createPromotion(PromotionRequest request);

  PromotionResponse updatePromotion(Long id, PromotionRequest request);

  PromotionResponse getPromotion(Long id);

  List<PromotionResponse> getAllPromotions();

  List<PromotionResponse> getActivePromotions();

  void deletePromotion(Long id);

  PromotionResponse togglePromotionStatus(Long id, boolean active);

  List<PromotionResponse> getPromotionsByProduct(Long productId);

  Promotion validateAndApplyPromotion(Long productId, String promotionCode);
}
