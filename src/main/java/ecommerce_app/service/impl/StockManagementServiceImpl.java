package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.response.*;
import ecommerce_app.entity.*;
import ecommerce_app.entity.ProductAttributeValue;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.repository.*;
import ecommerce_app.service.StockManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockManagementServiceImpl implements StockManagementService {

  private final ProductRepository productRepository;
  private final ProductVariantRepository variantRepository;
  private final VariantStockMovementRepository stockMovementRepository;

  // ── Adjust stock (variant-only, works for ALL products) ────────────────

  @Override
  @Transactional(rollbackFor = Exception.class)
  public VariantStockResponse adjustStock(StockAdjustmentRequest request, Long userId) {
    // Resolve which variant to adjust
    ProductVariant variant = getVariantForAdjustment(request);

    int before = variant.getStockQuantity();
    int after = calculateNewStock(before, request);

    // Validate stock-out operations
    if (request.getMovementType() == StockMovementType.OUT && before < request.getQuantity()) {
      throw new BadRequestException(
          String.format(
              "Insufficient stock for variant '%s'. Available: %d, Requested: %d",
              variant.getSku(), before, request.getQuantity()));
    }

    // Update stock
    variant.setStockQuantity(after);
    variantRepository.save(variant);

    // Record movement
    stockMovementRepository.save(
        VariantStockMovement.builder()
            .variant(variant)
            .movementType(request.getMovementType())
            .quantity(request.getQuantity())
            .quantityBefore(before)
            .quantityAfter(after)
            .referenceType(request.getReferenceType())
            .referenceId(request.getReferenceId())
            .note(request.getNote())
            .build());

    log.info(
        "Adjusted stock for variant '{}': {} → {} ({})",
        variant.getSku(),
        before,
        after,
        request.getMovementType());

    return VariantStockResponse.builder()
        .variantId(variant.getId())
        .variantSku(variant.getSku())
        .productId(variant.getProduct().getId())
        .productName(variant.getProduct().getName())
        .quantityBefore(before)
        .quantityAfter(after)
        .movementType(request.getMovementType())
        .stockStatus(variant.getStockStatus())
        .referenceType(request.getReferenceType())
        .adjustedAt(LocalDateTime.now())
        .build();
  }

  // ── Get stock summary ──────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public ProductStockResponse getProductStock(Long productId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

    List<ProductVariant> activeVariants =
        variantRepository.findByProductIdAndIsActiveTrue(productId);

    int totalStock = activeVariants.stream().mapToInt(ProductVariant::getStockQuantity).sum();

    List<VariantStockSummaryResponse> variantSummaries =
        activeVariants.stream()
            .map(
                v ->
                    VariantStockSummaryResponse.builder()
                        .variantId(v.getId())
                        .sku(v.getSku())
                        .stockQuantity(v.getStockQuantity())
                        .lowStockThreshold(v.getLowStockThreshold())
                        .stockStatus(v.getStockStatus())
                        .isActive(v.getIsActive())
                        .attributeValues(
                            v.getAttributeValues() != null
                                ? v.getAttributeValues().stream()
                                    .map(ProductAttributeValue::getValue)
                                    .toList()
                                : List.of())
                        .build())
            .toList();

    return ProductStockResponse.builder()
        .productId(product.getId())
        .productName(product.getName())
        .hasVariants(product.getHasVariants())
        .totalStock(totalStock)
        .variants(variantSummaries)
        .build();
  }

  // ── Stock movement history ─────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public Page<StockHistoryResponse> getStockHistory(
      Long productId,
      Long variantId,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Pageable pageable) {

    List<VariantStockMovement> movements;

    if (variantId != null) {
      // History for a specific variant
      movements =
          stockMovementRepository.findByVariantIdAndCreatedAtBetween(
              variantId, startDate, endDate, pageable);
    } else {
      // History for all variants of a product
      List<Long> variantIds =
          variantRepository.findByProductId(productId).stream().map(ProductVariant::getId).toList();

      movements =
          variantIds.isEmpty()
              ? List.of()
              : stockMovementRepository.findByVariantIdInAndCreatedAtBetween(
                  variantIds, startDate, endDate, pageable);
    }

    List<StockHistoryResponse> responses =
        movements.stream()
            .map(
                m ->
                    StockHistoryResponse.builder()
                        .id(m.getId())
                        .variantSku(m.getVariant().getSku())
                        .movementType(m.getMovementType())
                        .quantity(m.getQuantity())
                        .quantityBefore(m.getQuantityBefore())
                        .quantityAfter(m.getQuantityAfter())
                        .referenceType(m.getReferenceType())
                        .referenceId(m.getReferenceId()== null ? null : m.getReferenceId().toString())
                        .note(m.getNote())
                        .createdAt(m.getCreatedAt())
                        .createdBy(m.getCreatedBy())
                        .build())
            .toList();

    return new PageImpl<>(responses, pageable, responses.size());
  }

  // ── Low stock alerts ───────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<StockAlertResponse> getLowStockAlerts(int threshold) {
    return variantRepository.findByStockQuantityLessThanEqual(threshold).stream()
        .map(this::buildVariantAlert)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<StockAlertResponse> getLowStockAlertsForProduct(Long productId, int threshold) {
    return variantRepository
        .findByProductIdAndStockQuantityLessThanEqual(productId, threshold)
        .stream()
        .map(this::buildVariantAlert)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void bulkStockUpdate(List<StockAdjustmentRequest> requests, Long userId) {
    requests.forEach(request -> adjustStock(request, userId));
  }

  // ── Private helpers ────────────────────────────────────────────────────

  /**
   * For simple products (no variants), look up or create the default variant. For variant products,
   * variantId must be provided.
   */
  private ProductVariant getVariantForAdjustment(StockAdjustmentRequest request) {
    if (request.getVariantId() != null) {
      return variantRepository
          .findById(request.getVariantId())
          .orElseThrow(() -> new ResourceNotFoundException("Variant", request.getVariantId()));
    }

    // No variantId → must be a simple product → use its default variant
    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

    if (Boolean.TRUE.equals(product.getHasVariants())) {
      throw new BadRequestException("Product has variants — provide a variantId to adjust stock.");
    }

    return variantRepository
        .findByProductIdAndIsDefaultTrue(product.getId())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Product " + product.getId() + " has no default variant. Create one first."));
  }

  private int calculateNewStock(int before, StockAdjustmentRequest request) {
    return switch (request.getMovementType()) {
      case IN -> before + request.getQuantity();
      case OUT -> before - request.getQuantity();
      case ADJUSTMENT -> request.getQuantity();
      case RETURN -> before + request.getQuantity();
    };
  }

  private StockAlertResponse buildVariantAlert(ProductVariant v) {
    return StockAlertResponse.builder()
        .variantId(v.getId())
        .variantSku(v.getSku())
        .productId(v.getProduct().getId())
        .productName(v.getProduct().getName())
        .currentStock(v.getStockQuantity())
        .lowStockThreshold(v.getLowStockThreshold())
        .attributeValues(
            v.getAttributeValues() != null
                ? v.getAttributeValues().stream()
                    .map(ProductAttributeValue::getValue)
                    .collect(Collectors.toList())
                : List.of())
        .inStock(v.getInStock())
        .stockStatus(v.getStockStatus())
        .build();
  }
}
