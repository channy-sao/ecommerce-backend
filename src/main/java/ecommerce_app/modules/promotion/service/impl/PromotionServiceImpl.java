package ecommerce_app.modules.promotion.service.impl;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.product.model.dto.ProductResponse;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.promotion.model.dto.PromotionRequest;
import ecommerce_app.modules.promotion.model.dto.PromotionResponse;
import ecommerce_app.modules.promotion.model.entity.Promotion;
import ecommerce_app.modules.promotion.repository.PromotionRepository;
import ecommerce_app.modules.promotion.repository.PromotionUsageRepository;
import ecommerce_app.modules.promotion.service.PromotionService;
import ecommerce_app.util.ProductMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

  private final PromotionRepository promotionRepository;
  private final ProductRepository productRepository;
  private final PromotionUsageRepository promotionUsageRepository;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public PromotionResponse createPromotion(PromotionRequest request) {
    request.validate();

    if (request.getCode() != null && promotionRepository.existsByCode(request.getCode())) {
      throw new IllegalArgumentException("Promotion code already exists");
    }

    Promotion promotion = new Promotion();
    mapRequestToEntity(request, promotion);

    if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
      List<Product> products = productRepository.findAllById(request.getProductIds());
      if (products.size() != request.getProductIds().size()) {
        throw new EntityNotFoundException("Some products not found");
      }
      promotion.setProducts(products);
    }
    // apply to all products
    else {
      List<Product> allProducts = productRepository.findAll();
      promotion.setProducts(allProducts);
    }

    promotion.setMinPurchaseAmount(request.getMinPurchaseAmount());
    Promotion savedPromotion = promotionRepository.save(promotion);
    log.info("Created promotion: {}", savedPromotion.getName());

    return mapToResponse(savedPromotion);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
    request.validate();

    Promotion promotion = getById(id);

    if (request.getCode() != null
        && !request.getCode().equals(promotion.getCode())
        && promotionRepository.existsByCode(request.getCode())) {
      throw new IllegalArgumentException("Promotion code already exists");
    }

    mapRequestToEntity(request, promotion);

    if (request.getProductIds() != null) {
      List<Product> products = productRepository.findAllById(request.getProductIds());
      if (products.size() != request.getProductIds().size()) {
        throw new EntityNotFoundException("Some products not found");
      }
      promotion.setProducts(products);
    }

    Promotion updatedPromotion = promotionRepository.save(promotion);
    log.info("Updated promotion: {}", updatedPromotion.getName());

    return mapToResponse(updatedPromotion);
  }

  @Transactional(readOnly = true)
  @Override
  public PromotionResponse getPromotion(Long id) {
    Promotion promotion = getById(id);

    return mapToResponse(promotion);
  }

  @Transactional(readOnly = true)
  @Override
  public List<PromotionResponse> getAllPromotions() {
    return promotionRepository.findAll().stream().map(this::mapToResponse).toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<PromotionResponse> getActivePromotions() {
    return promotionRepository.findActivePromotions(LocalDateTime.now()).stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deletePromotion(Long id) {
    Promotion promotion = getById(id);

    // Remove associations
    promotion.getProducts().forEach(product -> product.getPromotions().remove(promotion));
    promotion.getProducts().clear();

    promotionRepository.delete(promotion);
    log.info("Deleted promotion: {}", promotion.getName());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public PromotionResponse togglePromotionStatus(Long id, boolean active) {
    Promotion promotion = getById(id);

    promotion.setActive(active);
    Promotion updatedPromotion = promotionRepository.save(promotion);

    log.info("{} promotion: {}", active ? "Activated" : "Deactivated", promotion.getName());

    return mapToResponse(updatedPromotion);
  }

  @Transactional(readOnly = true)
  @Override
  public List<PromotionResponse> getPromotionsByProduct(Long productId) {
    return promotionRepository
        .findActivePromotionsByProductId(productId, LocalDateTime.now())
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  private Promotion getById(Long id) {
    return promotionRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Promotion"));
  }

  @Override
  public Promotion validateAndApplyPromotion(Long productId, String promotionCode) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product"));

    Promotion promotion =
        promotionRepository
            .findByCode(promotionCode)
            .orElseThrow(() -> new EntityNotFoundException("Promotion not found"));

    // Check if promotion is active
    if (!promotion.getActive()) {
      throw new IllegalStateException("Promotion is not active");
    }

    // Check date validity
    LocalDateTime now = LocalDateTime.now();
    if (promotion.getStartAt() != null && now.isBefore(promotion.getStartAt())) {
      throw new IllegalStateException("Promotion has not started yet");
    }

    if (promotion.getEndAt() != null && now.isAfter(promotion.getEndAt())) {
      throw new IllegalStateException("Promotion has expired");
    }

    // Check if product is applicable
    if (!promotion.getProducts().isEmpty() && !promotion.getProducts().contains(product)) {
      throw new IllegalStateException("Promotion is not applicable to this product");
    }

    // Check usage limit
    if (promotion.getMaxUsage() != null) {
      Long currentUsage = promotionUsageRepository.countByPromotionId(promotion.getId());
      if (currentUsage >= promotion.getMaxUsage()) {
        throw new IllegalStateException("Promotion usage limit reached");
      }
    }

    return promotion;
  }

  private void mapRequestToEntity(PromotionRequest request, Promotion promotion) {
    promotion.setCode(request.getCode());
    promotion.setName(request.getName());
    promotion.setDiscountType(request.getDiscountType());
    promotion.setDiscountValue(request.getDiscountValue());
    promotion.setBuyQuantity(request.getBuyQuantity());
    promotion.setGetQuantity(request.getGetQuantity());
    promotion.setActive(request.getActive());
    promotion.setStartAt(request.getStartAt());
    promotion.setEndAt(request.getEndAt());
    promotion.setMaxUsage(request.getMaxUsage());
    promotion.setMinPurchaseAmount(request.getMinPurchaseAmount());
  }

  private PromotionResponse mapToResponse(Promotion promotion) {
    PromotionResponse response = new PromotionResponse();
    response.setId(promotion.getId());
    response.setCode(promotion.getCode());
    response.setName(promotion.getName());
    response.setDiscountType(promotion.getDiscountType());
    response.setDiscountValue(promotion.getDiscountValue());
    response.setBuyQuantity(promotion.getBuyQuantity());
    response.setGetQuantity(promotion.getGetQuantity());
    response.setActive(promotion.getActive());
    response.setStartAt(promotion.getStartAt());
    response.setEndAt(promotion.getEndAt());
    response.setMaxUsage(promotion.getMaxUsage());
    response.setCreatedAt(promotion.getCreatedAt());
    response.setUpdatedAt(promotion.getUpdatedAt());

    // Get current usage
    Long currentUsage = promotionUsageRepository.countByPromotionId(promotion.getId());
    response.setCurrentUsage(currentUsage.intValue());

    // Map products
    if (promotion.getProducts() != null) {
      List<ProductResponse> productInfos =
          promotion.getProducts().stream()
              .map(ProductMapper::toProductResponse)
              .collect(Collectors.toList());
      response.setProducts(productInfos);
    }

    return response;
  }
}
