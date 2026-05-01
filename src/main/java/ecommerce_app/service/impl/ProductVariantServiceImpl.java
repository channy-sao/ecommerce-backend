package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.dto.request.ProductVariantRequest;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.response.ProductVariantResponse;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.ProductAttributeValue;
import ecommerce_app.entity.ProductVariant;
import ecommerce_app.entity.VariantStockMovement;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.DuplicateResourceException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.ProductVariantMapper;
import ecommerce_app.repository.ProductAttributeValueRepository;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.repository.ProductVariantRepository;
import ecommerce_app.repository.VariantStockMovementRepository;
import ecommerce_app.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantServiceImpl implements ProductVariantService {

  private final ProductVariantRepository variantRepository;
  private final ProductRepository productRepository;
  private final ProductAttributeValueRepository attributeValueRepository;
  private final VariantStockMovementRepository stockMovementRepository;
  private final ProductVariantMapper variantMapper;

  // ── READ ─────────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  @Override
  public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
    ensureProductExists(productId);
    return variantMapper.toResponseList(variantRepository.findByProductId(productId));
  }

  @Transactional(readOnly = true)
  @Override
  public ProductVariantResponse getVariantById(Long variantId) {
    return variantMapper.toResponse(findVariantById(variantId));
  }

  // ── CREATE ───────────────────────────────────────────────────────────────

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ProductVariantResponse createVariant(Long productId, ProductVariantRequest request) {
    Product product = findProductById(productId);

    if (!Boolean.TRUE.equals(product.getHasVariants())) {
      throw new BadRequestException(
          "Product "
              + productId
              + " is not a variant product. "
              + "Set hasVariants = true on the product first.");
    }

    String sku = normalizeSku(request.getSku());
    if (variantRepository.existsBySku(sku)) {
      throw new DuplicateResourceException("ProductVariant", "sku", sku);
    }

    List<ProductAttributeValue> attrValues = resolveAttrValues(request.getAttributeValueIds());

    ProductVariant variant = variantMapper.toEntity(request);
    variant.setSku(sku);
    variant.setProduct(product);
    variant.setAttributeValues(attrValues);
    variant.setStockMovements(new ArrayList<>());

    ProductVariant saved = variantRepository.save(variant);

    // Record initial IN movement if stock > 0
    if (saved.getStockQuantity() > 0) {
      recordMovement(
          saved,
          StockMovementType.IN,
          saved.getStockQuantity(),
          0,
          saved.getStockQuantity(),
          null,
          "INITIAL",
          "Initial stock on variant creation");
    }

    log.info("Created variant SKU='{}' for product {}", saved.getSku(), productId);
    return variantMapper.toResponse(saved);
  }

  // ── UPDATE ───────────────────────────────────────────────────────────────

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ProductVariantResponse updateVariant(Long variantId, ProductVariantRequest request) {
    ProductVariant variant = findVariantById(variantId);

    String newSku = normalizeSku(request.getSku());
    if (!variant.getSku().equals(newSku)
        && variantRepository.existsBySkuAndIdNot(newSku, variantId)) {
      throw new DuplicateResourceException("ProductVariant", "sku", newSku);
    }

    // Update scalar fields via mapper (SKU, price, lowStockThreshold)
    variantMapper.updateEntity(variant, request);
    variant.setSku(newSku);

    // Replace attribute values if provided
    if (request.getAttributeValueIds() != null) {
      // Clear existing attributes
      variant.getAttributeValues().clear();

      // Add new attributes
      List<ProductAttributeValue> newAttributes = resolveAttrValues(request.getAttributeValueIds());
      variant.getAttributeValues().addAll(newAttributes);
    }

    log.info("Updated variant {}", variantId);
    ProductVariant saved = variantRepository.save(variant);
    return variantMapper.toResponse(saved);
  }

  // ── DELETE (soft) ─────────────────────────────────────────────────────────

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteVariant(Long variantId) {
    ProductVariant variant = findVariantById(variantId);
    variant.setIsActive(false);
    variantRepository.save(variant);
    log.info("Deactivated variant {}", variantId);
  }

  // ── STOCK ADJUSTMENT ──────────────────────────────────────────────────────
  // Called by: ProductImportServiceImpl (IN), OrderServiceImpl can also call here

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ProductVariantResponse adjustStock(StockAdjustmentRequest request) {
    if (request.getQuantity() <= 0) {
      throw new BadRequestException("Quantity must be positive");
    }

    ProductVariant variant = findVariantById(request.getVariantId());
    int before = variant.getStockQuantity();
    int after;

    switch (request.getMovementType()) {
      case IN, RETURN -> after = before + request.getQuantity();
      case OUT -> {
        if (before < request.getQuantity()) {
          throw new BadRequestException(
              "Insufficient stock for variant '"
                  + variant.getSku()
                  + "': available="
                  + before
                  + ", requested="
                  + request.getQuantity());
        }
        after = before - request.getQuantity();
      }
      case ADJUSTMENT -> after = request.getQuantity(); // set absolute value
      default ->
          throw new BadRequestException("Unknown movement type: " + request.getMovementType());
    }

    variant.setStockQuantity(after);
    variantRepository.save(variant);

    recordMovement(
        variant,
        request.getMovementType(),
        request.getQuantity(),
        before,
        after,
        request.getReferenceId(),
        request.getReferenceType(),
        request.getNote());

    log.info(
        "Stock adjusted for variant '{}': {} → {} ({})",
        variant.getSku(),
        before,
        after,
        request.getMovementType());

    return variantMapper.toResponse(variant);
  }

  // ── Bulk create ───────────────────────────────────────────────────────────

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<ProductVariantResponse> createVariants(Long productId, List<ProductVariantRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      throw new BadRequestException("At least one variant is required");
    }
    return requests.stream()
            .map(request -> createVariant(productId, request))
            .toList();
  }

// ── Stock history ─────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  @Override
  public List<VariantStockMovement> getStockHistory(Long variantId) {
    findVariantById(variantId); // ensure variant exists
    return stockMovementRepository
            .findByVariantIdOrderByCreatedAtDesc(variantId);
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private Product findProductById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product", id));
  }

  private void ensureProductExists(Long id) {
    findProductById(id); // throws if missing
  }

  private ProductVariant findVariantById(Long id) {
    return variantRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", id));
  }

  private List<ProductAttributeValue> resolveAttrValues(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return new ArrayList<>();
    return ids.stream()
        .map(
            id ->
                attributeValueRepository
                    .findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", id)))
        .toList();
  }

  private String normalizeSku(String sku) {
    return sku == null ? null : sku.trim().toUpperCase();
  }

  private void recordMovement(
      ProductVariant variant,
      StockMovementType type,
      int quantity,
      int before,
      int after,
      Long referenceId,
      String referenceType,
      String note) {
    stockMovementRepository.save(
        VariantStockMovement.builder()
            .variant(variant)
            .movementType(type)
            .quantity(quantity)
            .quantityBefore(before)
            .quantityAfter(after)
            .referenceId(referenceId)
            .referenceType(referenceType)
            .note(note)
            .build());
  }
}
