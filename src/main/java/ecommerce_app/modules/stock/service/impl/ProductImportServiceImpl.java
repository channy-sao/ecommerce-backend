package ecommerce_app.modules.stock.service.impl;

import ecommerce_app.infrastructure.annotation.LogExecutionTime;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.stock.model.dto.ProductImportRequest;
import ecommerce_app.modules.stock.model.dto.ProductImportResponse;
import ecommerce_app.modules.stock.model.entity.ProductImport;
import ecommerce_app.modules.stock.model.entity.Stock;
import ecommerce_app.modules.stock.repository.ProductImportRepository;
import ecommerce_app.modules.stock.repository.StockRepository;
import ecommerce_app.modules.stock.service.ProductImportService;
import ecommerce_app.modules.stock.service.StockService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
  private final ModelMapper modelMapper;

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
    ProductImport importRecord = new ProductImport();
    importRecord.setProduct(product);
    importRecord.setQuantity(productImportRequest.getQuantity());
    importRecord.setUnitPrice(productImportRequest.getUnitPrice());
    importRecord.setTotalAmount(
        productImportRequest
            .getUnitPrice()
            .multiply(BigDecimal.valueOf(productImportRequest.getQuantity())));
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
        .map(productImport -> modelMapper.map(productImport, ProductImportResponse.class))
        .toList();
  }

  @Override
  public List<ProductImportResponse> getProductImportsByProductId(Long productId) {
    log.info("Retrieving all products by productId");
    return productImportRepository.findByProductId(productId).stream()
        .map(productImport -> modelMapper.map(productImport, ProductImportResponse.class))
        .toList();
  }
}
