package ecommerce_app.modules.stock.service.impl;

import ecommerce_app.infrastructure.annotation.LogExecutionTime;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.mapper.ProductImportMapper;
import ecommerce_app.infrastructure.model.entity.BaseAuditingEntity;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.stock.model.dto.ProductImportFilterRequest;
import ecommerce_app.modules.stock.model.dto.ProductImportHistoryByProductResponse;
import ecommerce_app.modules.stock.model.dto.ProductImportRequest;
import ecommerce_app.modules.stock.model.dto.ProductImportResponse;
import ecommerce_app.modules.stock.model.entity.ProductImport;
import ecommerce_app.modules.stock.model.entity.Stock;
import ecommerce_app.modules.stock.repository.ProductImportRepository;
import ecommerce_app.modules.stock.repository.StockRepository;
import ecommerce_app.modules.stock.service.ProductImportService;
import ecommerce_app.modules.stock.service.StockService;
import ecommerce_app.modules.stock.specification.ProductImportSpecification;
import ecommerce_app.util.ProductMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class ProductImportServiceImpl implements ProductImportService {
  private final StockService stockService;
  private final StockRepository stockRepository;
  private final ProductRepository productRepository;
  private final ProductImportRepository productImportRepository;
  private final ProductImportMapper productImportMapper;

  private static ProductImport getPrepareProductImport(
      ProductImportRequest productImportRequest, Product product) {
    ProductImport importRecord = new ProductImport();
    importRecord.setProduct(product);
    importRecord.setQuantity(productImportRequest.getQuantity());
    importRecord.setUnitPrice(productImportRequest.getUnitPrice());
    importRecord.setTotalAmount(
        productImportRequest
            .getUnitPrice()
            .multiply(BigDecimal.valueOf(productImportRequest.getQuantity())));
    importRecord.setSupplierName(productImportRequest.getSupplierName());
    importRecord.setSupplierAddress(productImportRequest.getSupplierAddress());
    importRecord.setSupplierPhone(productImportRequest.getSupplierPhone());
    return importRecord;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void importProduct(ProductImportRequest productImportRequest) {
    log.info("Importing product to stock");

    Product product =
        productRepository
            .findById(productImportRequest.getProductId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Product", productImportRequest.getProductId()));

    // 1. Save history
    ProductImport importRecord = getPrepareProductImport(productImportRequest, product);
    productImportRepository.save(importRecord);

    // 2. Update stock
    stockService.increaseStock(
        productImportRequest.getProductId(), productImportRequest.getQuantity());
  }

  @LogExecutionTime
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateImportProduct(Long id, ProductImportRequest productImportRequest) {
    ProductImport importRecord =
        productImportRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ProductImport", id));

    Long productId = importRecord.getProduct().getId();
    int oldQty = importRecord.getQuantity();
    int newQty = productImportRequest.getQuantity();
    int diff = newQty - oldQty;

    // Adjust stock only if needed
    if (diff != 0) {
      Stock stock =
          stockRepository
              .findByProductId(productId)
              .orElseThrow(
                  () -> new IllegalStateException("Stock not found for product " + productId));

      int newStock = stock.getQuantity() + diff;
      if (newStock < 0) {
        log.warn("Stock is negative");
        throw new IllegalStateException("Stock cannot be negative");
      }

      stock.setQuantity(newStock);
      stockRepository.save(stock);
    }
    // Update import fields
    importRecord.setQuantity(newQty);
    importRecord.setUnitPrice(productImportRequest.getUnitPrice());
    importRecord.setTotalAmount(
        productImportRequest
            .getUnitPrice()
            .multiply(BigDecimal.valueOf(productImportRequest.getQuantity())));
    importRecord.setRemark(productImportRequest.getRemark()); // ✅ REMARK UPDATED

    productImportRepository.save(importRecord);
    log.info("Updated product to stock");
  }

  @Override
  public List<ProductImportResponse> getProductImports() {
    log.info("Retrieving all products");
    return productImportRepository.findAll().stream()
        .map(productImportMapper::toProductImportResponse)
        .toList();
  }

  @Override
  public List<ProductImportResponse> getProductImportsByProductId(Long productId) {
    log.info("Retrieving all products imported by productId");
    return productImportRepository.findByProductId(productId).stream()
        .map(productImportMapper::toProductImportResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  @LogExecutionTime
  @Override
  public ProductImportHistoryByProductResponse getProductImportHistoryByProductId(Long productId) {
    final var productImportHistory = productImportRepository.findByProductId(productId);
    if (productImportHistory.isEmpty()) {
      log.warn("Product imported not found for productId {}", productId);
      return null;
    }
    final var productImportHistoryResponse = new ProductImportHistoryByProductResponse();
    // product representation
    productImportHistoryResponse.setProduct(
        ProductMapper.toProductResponse(productImportHistory.getFirst().getProduct()));

    // history
    // Group imports by date and create response map
    Map<LocalDateTime, ProductImportResponse> productImports =
        productImportHistory.stream()
            .collect(
                Collectors.toMap(
                    BaseAuditingEntity::getCreatedAt,
                    productImportMapper::toProductImportResponse,
                    (existing, replacement) -> existing,
                    () -> new TreeMap<>(Comparator.reverseOrder()) // Sorted map by date
                    ));
    productImportHistoryResponse.setProductImports(productImports);
    return productImportHistoryResponse;
  }

  @Transactional(readOnly = true)
  @Override
  public Page<ProductImportResponse> getImportListing(ProductImportFilterRequest filter) {
    // Build specification
    Specification<ProductImport> spec = ProductImportSpecification.filter(filter);

    Sort sort = Sort.by(filter.getSortDirection(), filter.getSortBy());

    if (!filter.isPaged()) {
      final List<ProductImport> productImportList = productImportRepository.findAll(spec, sort);
      final List<ProductImportResponse> listingResponses =
          productImportList.stream().map(productImportMapper::toProductImportResponse).toList();
      return new PageImpl<>(listingResponses);
    }
    // page start from 0
    final var pageable = PageRequest.of(filter.getPage() - 1, filter.getPageSize(), sort);
    // Get paginated results
    Page<ProductImport> importPage = productImportRepository.findAll(spec, pageable);

    return importPage.map(productImportMapper::toProductImportResponse);
  }
}
