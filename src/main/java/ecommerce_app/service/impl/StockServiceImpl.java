package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.StockStatus;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.StockMapper;
import ecommerce_app.entity.Product;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.dto.response.StockResponse;
import ecommerce_app.entity.Stock;
import ecommerce_app.repository.StockRepository;
import ecommerce_app.service.StockService;
import java.util.List;

import ecommerce_app.specification.StockSpecification;
import ecommerce_app.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
  private final StockRepository stockRepository;
  private final ProductRepository productRepository;
  private final StockMapper stockMapper;

  @Transactional(readOnly = true)
  @Override
  public StockResponse getByProductId(Long productId) {
    log.info("Get Stock By ProductId: {}", productId);
    return stockMapper.toStockResponse(stockRepository.getByProductId(productId));
  }

  @Override
  public void increaseStock(Long productId, int quantity) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

    Stock stock =
        stockRepository
            .findByProductId(productId)
            .orElseGet(
                () -> stockRepository.save(Stock.builder().product(product).quantity(0).build()));
    // increase quantity
    stock.setQuantity(stock.getQuantity() + quantity);
    stockRepository.save(stock);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void adjustStock(Long productId, int quantity) {
    // Find existing stock
    final var stock =
        stockRepository
            .findByProductId(productId)
            .orElseThrow(
                () -> new IllegalStateException("Stock not found for product: " + productId));

    // Calculate new quantity
    int newQuantity = stock.getQuantity() + quantity;

    // Prevent negative stock
    if (newQuantity < 0) {
      throw new IllegalStateException(
          "Stock cannot be negative. Current stock: "
              + stock.getQuantity()
              + ", adjustment: "
              + quantity);
    }

    // Update stock
    stock.setQuantity(newQuantity);

    stockRepository.save(stock);

    // Convert to response
    StockResponse.builder()
        .id(stock.getId())
        .product(ProductMapper.toProductResponse(stock.getProduct()))
        .quantity(stock.getQuantity())
        .updatedAt(stock.getUpdatedAt())
        .build();
  }

  @Transactional(readOnly = true)
  @Override
  public Page<StockResponse> getStocks(
          boolean isPaged,
          int page,
          int pageSize,
          String sortBy,
          Sort.Direction sortDirection,
          String filter, StockStatus status) {
    log.info("Get Stock List");
    Specification<Stock> specification = Specification.allOf();

    if (filter != null && !filter.trim().isEmpty()) {
      specification = specification.and(StockSpecification.filter(filter));
    }
    if(status != null) {
      specification = specification.and(StockSpecification.hasStockStatus(status));
    }
    Sort sort = Sort.by(sortDirection, sortBy);
    if (!isPaged) {
      List<StockResponse> stocks =
          stockRepository.findAll(specification, sort).stream()
              .map(stockMapper::toStockResponse)
              .toList();
      return new PageImpl<>(stocks);
    }
    PageRequest pageRequest = PageRequest.of(page - 1, pageSize, sort);
    final var stocks = stockRepository.findAll(specification, pageRequest);
    return stocks.map(stockMapper::toStockResponse);
  }
}
