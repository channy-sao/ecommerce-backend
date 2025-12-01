package ecommerce_app.modules.stock.service.impl;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.stock.model.dto.StockResponse;
import ecommerce_app.modules.stock.model.entity.Stock;
import ecommerce_app.modules.stock.repository.StockRepository;
import ecommerce_app.modules.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
  private final StockRepository stockRepository;
  private final ModelMapper modelMapper;
  private final ProductRepository productRepository;

  @Override
  public StockResponse getByProductId(Long productId) {
    log.info("Get Stock By ProductId: {}", productId);
    return modelMapper.map(stockRepository.getByProductId(productId), StockResponse.class);
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
        .productId(stock.getProduct().getId())
        .quantity(stock.getQuantity())
        .updatedAt(stock.getUpdatedAt())
        .build();
  }

  @Override
  public List<StockResponse> getStocks() {
    log.info("Get Stock List");
    final var stocks = stockRepository.findAll();
    return stocks.stream().map(stock -> modelMapper.map(stock, StockResponse.class)).toList();
  }
}
