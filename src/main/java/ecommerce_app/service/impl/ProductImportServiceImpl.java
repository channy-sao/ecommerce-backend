package ecommerce_app.service.impl;

import ecommerce_app.annotation.LogExecutionTime;
import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.dto.request.ProductImportFilterRequest;
import ecommerce_app.dto.request.ProductImportRequest;
import ecommerce_app.dto.request.ProductVariantRequest;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.response.ProductImportHistoryByProductResponse;
import ecommerce_app.dto.response.ProductImportResponse;
import ecommerce_app.entity.*;
import ecommerce_app.entity.base.TimeAuditableEntity;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.ProductImportMapper;
import ecommerce_app.repository.*;
import ecommerce_app.service.ProductImportService;
import ecommerce_app.service.StockManagementService;
import ecommerce_app.specification.ProductImportSpecification;
import ecommerce_app.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImportServiceImpl implements ProductImportService {

  private final ProductRepository productRepository;
  private final ProductImportRepository productImportRepository;
  private final ProductImportMapper productImportMapper;
  private final ProductVariantRepository productVariantRepository;
  private final StockManagementService stockManagementService;
  private final ProductVariantRepository variantRepository;

  private static final int MAX_UNPAGED_RESULTS = 100;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void importProduct(ProductImportRequest request, Long userId) {
    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

    ProductVariant variant = resolveVariant(product, request.getVariantId());

    // Save import record first
    ProductImport importRecord = buildImportRecord(request, product, variant);
    importRecord = productImportRepository.save(importRecord);

    // Then adjust stock
    stockManagementService.adjustStock(
        StockAdjustmentRequest.builder()
            .productId(product.getId())
            .variantId(variant.getId())
            .movementType(StockMovementType.IN)
            .quantity(request.getQuantity())
            .referenceType("IMPORT")
            .referenceId(importRecord.getId())
            .note(request.getRemark())
            .build(),
        userId);

    log.info(
        "Imported {} units for variant {} (product {})",
        request.getQuantity(),
        variant.getSku(),
        product.getId());
  }

  @LogExecutionTime
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateImportProduct(Long importId, ProductImportRequest request) {
    validateUpdateRequest(request);

    ProductImport importRecord =
        productImportRepository
            .findById(importId)
            .orElseThrow(() -> new ResourceNotFoundException("ProductImport", importId));

    int oldQty = importRecord.getQuantity();
    int newQty = request.getQuantity();
    int diff = newQty - oldQty;

    if (diff != 0) {
      adjustStockForUpdate(importRecord, importId, oldQty, newQty, diff);
    }

    updateImportRecordFields(importRecord, request);
    productImportRepository.save(importRecord);

    log.info("Updated import record {} (qty {} → {})", importId, oldQty, newQty);
  }

  @Override
  public List<ProductImportResponse> getProductImports() {
    return productImportRepository.findAll().stream()
        .map(productImportMapper::toProductImportResponse)
        .toList();
  }

  @Override
  public List<ProductImportResponse> getProductImportsByProductId(Long productId) {
    return productImportRepository.findByProductId(productId).stream()
        .map(productImportMapper::toProductImportResponse)
        .toList();
  }

  @LogExecutionTime
  @Override
  public ProductImportHistoryByProductResponse getProductImportHistoryByProductId(Long productId) {
    List<ProductImport> history = productImportRepository.findByProductId(productId);

    ProductImportHistoryByProductResponse response = new ProductImportHistoryByProductResponse();

    if (history.isEmpty()) {
      productRepository
          .findById(productId)
          .ifPresent(product -> response.setProduct(ProductMapper.toProductResponse(product)));
      response.setProductImports(new TreeMap<>());
      return response;
    }

    response.setProduct(ProductMapper.toProductResponse(history.getFirst().getProduct()));

    Map<LocalDateTime, ProductImportResponse> imports =
        history.stream()
            .collect(
                Collectors.toMap(
                    TimeAuditableEntity::getCreatedAt,
                    productImportMapper::toProductImportResponse,
                    (a, b) -> a,
                    TreeMap::new));
    response.setProductImports(imports);
    return response;
  }

  @Override
  public Page<ProductImportResponse> getImportListing(ProductImportFilterRequest filter) {
    Specification<ProductImport> spec = ProductImportSpecification.filter(filter);
    Sort sort = Sort.by(filter.getSortDirection(), filter.getSortBy());

    if (!filter.isPaged()) {
      PageRequest pageable = PageRequest.of(0, MAX_UNPAGED_RESULTS, sort);
      return productImportRepository
          .findAll(spec, pageable)
          .map(productImportMapper::toProductImportResponse);
    }

    PageRequest pageable = PageRequest.of(filter.getPage() - 1, filter.getPageSize(), sort);
    return productImportRepository
        .findAll(spec, pageable)
        .map(productImportMapper::toProductImportResponse);
  }

  @Override
  public ProductImportResponse getImportById(Long id) {
    ProductImport importRecord =
        productImportRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ProductImport", id));
    return productImportMapper.toProductImportResponse(importRecord);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteImport(Long id) {
    ProductImport importRecord =
        productImportRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ProductImport", id));

    ProductVariant variant = importRecord.getVariant();
    if (variant == null) {
      variant =
          variantRepository
              .findByProductIdAndIsDefaultTrue(importRecord.getProduct().getId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Default variant for product: " + importRecord.getProduct().getId()));
    }

    int importedQty = importRecord.getQuantity();
    int currentStock = variant.getStockQuantity();

    // ✅ Calculate what stock SHOULD be after removing this import
    // If nothing was sold: newStock = currentStock - importedQty
    // If some were sold: we can only go back to what was there before import
    int newStock = Math.max(0, currentStock - importedQty);

    // ✅ Use ADJUSTMENT to set stock to the calculated value
    stockManagementService.adjustStock(
        StockAdjustmentRequest.builder()
            .productId(importRecord.getProduct().getId())
            .variantId(variant.getId())
            .movementType(StockMovementType.ADJUSTMENT)
            .quantity(newStock)
            .referenceType("IMPORT_DELETED")
            .referenceId(importRecord.getId())
            .note(
                String.format(
                    "Import #%d deleted - stock adjusted from %d to %d (reversed %d units)",
                    importRecord.getId(), currentStock, newStock, currentStock - newStock))
            .build(),
        null);

    productImportRepository.delete(importRecord);
    log.info(
        "Deleted import record {}. Stock adjusted from {} to {} for variant '{}'",
        id,
        currentStock,
        newStock,
        variant.getSku());
  }

  // ── Private helpers ────────────────────────────────────────────────────

  private ProductVariant resolveVariant(Product product, Long variantId) {
    if (variantId != null) {
      ProductVariant variant =
          productVariantRepository
              .findById(variantId)
              .orElseThrow(() -> new ResourceNotFoundException("Variant", variantId));

      if (!variant.getProduct().getId().equals(product.getId())) {
        throw new BadRequestException("Variant does not belong to product " + product.getId());
      }
      return variant;
    }

    return productVariantRepository
        .findByProductIdAndIsDefaultTrue(product.getId())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Product " + product.getId() + " has no default variant"));
  }

  private ProductImport buildImportRecord(
      ProductImportRequest request, Product product, ProductVariant variant) {
    return ProductImport.builder()
        .product(product)
        .variant(variant)
        .quantity(request.getQuantity())
        .unitPrice(request.getUnitPrice())
        .totalAmount(request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
        .supplierName(request.getSupplierName())
        .supplierAddress(request.getSupplierAddress())
        .supplierPhone(request.getSupplierPhone())
        .remark(request.getRemark())
        .build();
  }

  private void validateUpdateRequest(ProductImportRequest request) {
    if (request.getQuantity() <= 0) {
      throw new BadRequestException("Quantity must be positive");
    }
    if (request.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException("Unit price must be positive");
    }
  }

  private void adjustStockForUpdate(
      ProductImport importRecord, Long importId, int oldQty, int newQty, int diff) {
    ProductVariant variant = importRecord.getVariant();
    if (variant == null) {
      throw new IllegalStateException("Import record has no variant linked");
    }

    StockMovementType movementType = diff > 0 ? StockMovementType.IN : StockMovementType.OUT;

    stockManagementService.adjustStock(
        StockAdjustmentRequest.builder()
            .productId(importRecord.getProduct().getId())
            .variantId(variant.getId())
            .movementType(movementType)
            .quantity(Math.abs(diff))
            .referenceType("IMPORT_UPDATE")
            .referenceId(importId)
            .note(String.format("Import #%d corrected from %d to %d", importId, oldQty, newQty))
            .build(),
        null);
  }

  private void updateImportRecordFields(ProductImport importRecord, ProductImportRequest request) {
    importRecord.setQuantity(request.getQuantity());
    importRecord.setUnitPrice(request.getUnitPrice());
    importRecord.setTotalAmount(
        request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
    importRecord.setSupplierName(request.getSupplierName());
    importRecord.setSupplierAddress(request.getSupplierAddress());
    importRecord.setSupplierPhone(request.getSupplierPhone());
    importRecord.setRemark(request.getRemark());
  }
}
