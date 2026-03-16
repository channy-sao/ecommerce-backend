package ecommerce_app.service.impl;

import ecommerce_app.dto.request.PromotionRequest;
import ecommerce_app.dto.response.PromotionResponse;
import ecommerce_app.dto.response.SimpleProductResponse;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.Promotion;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.repository.PromotionRepository;
import ecommerce_app.repository.PromotionUsageRepository;
import ecommerce_app.service.PromotionService;
import ecommerce_app.specification.PromotionSpecification;
import ecommerce_app.util.ProductMapper;
import ecommerce_app.util.PromotionValidator;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

  private final PromotionRepository promotionRepository;
  private final ProductRepository productRepository;
  private final PromotionUsageRepository promotionUsageRepository;
  private final PromotionNotificationService promotionNotificationService;
  private final PromotionValidator promotionValidator;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public PromotionResponse createPromotion(PromotionRequest request) {
    promotionValidator.validate(request);

    if (request.getCode() != null && promotionRepository.existsByCode(request.getCode())) {
      throw new IllegalArgumentException("Promotion code already exists");
    }

    Promotion promotion = new Promotion();
    mapRequestToEntity(request, promotion);
    if (!request.isApplyToAll() && !CollectionUtils.isEmpty(request.getProductIds())) {
      List<Product> products = productRepository.findAllById(request.getProductIds());
      promotion.setProducts(products);
    }
    // apply to all products
    else {
      List<Product> allProducts = productRepository.findAll();
      promotion.setProducts(allProducts);
    }
    promotion.setApplyToAll(request.isApplyToAll());

    promotion.setMinPurchaseAmount(request.getMinPurchaseAmount());
    Promotion savedPromotion = promotionRepository.save(promotion);
    log.info("Created promotion: {}", savedPromotion.getName());

    promotionNotificationService.notifyNewPromotion(savedPromotion);

    return mapToResponse(savedPromotion);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
    promotionValidator.validate(request);

    Promotion promotion = getById(id);

    if (request.getCode() != null
        && !request.getCode().equals(promotion.getCode())
        && promotionRepository.existsByCode(request.getCode())) {
      throw new IllegalArgumentException("Promotion code already exists");
    }

    mapRequestToEntity(request, promotion);
    if (!request.isApplyToAll() && !CollectionUtils.isEmpty(request.getProductIds())) {
      List<Product> products = productRepository.findAllById(request.getProductIds());
      promotion.setProducts(products);
    } else {
      List<Product> allProducts = productRepository.findAll();
      promotion.setProducts(allProducts);
    }

    promotion.setApplyToAll(request.isApplyToAll());

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
      long currentUsage = promotionUsageRepository.countByPromotionId(promotion.getId());
      if (currentUsage >= promotion.getMaxUsage()) {
        throw new IllegalStateException("Promotion usage limit reached");
      }
    }

    return promotion;
  }

  @Transactional(readOnly = true)
  @Override
  public Page<PromotionResponse> getPromotionsByPage(
      String query, Boolean active, String discountType, Integer page, Integer pageSize) {
    Specification<Promotion> specification =
        PromotionSpecification.filter(query, active, discountType);
    PageRequest pageRequest =
        PageRequest.of(
            page - 1,
            pageSize,
            Sort.by(Sort.Direction.DESC, "startAt")); // page - 1: Spring is 0-based
    return promotionRepository.findAll(specification, pageRequest).map(this::mapToResponse);
  }

  private void mapRequestToEntity(PromotionRequest request, Promotion promotion) {
    promotion.setCode(request.getCode()== null ? null : request.getCode().trim());
    promotion.setName(request.getName().trim());
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
    response.setMinPurchaseAmount(promotion.getMinPurchaseAmount());
    response.setCreatedAt(promotion.getCreatedAt());
    response.setUpdatedAt(promotion.getUpdatedAt());

    // Get current usage
    long currentUsage = promotionUsageRepository.countByPromotionId(promotion.getId());
    response.setCurrentUsage((int) currentUsage);

    // Map products
    if (promotion.getProducts() != null) {
      List<SimpleProductResponse> productInfos =
          promotion.getProducts().stream().map(ProductMapper::toSimpleProductResponse).toList();
      response.setProducts(productInfos);
    }

    return response;
  }
}
