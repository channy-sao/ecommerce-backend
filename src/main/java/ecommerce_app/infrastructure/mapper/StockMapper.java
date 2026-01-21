package ecommerce_app.infrastructure.mapper;

import ecommerce_app.modules.stock.model.dto.StockResponse;
import ecommerce_app.modules.stock.model.entity.Stock;
import ecommerce_app.util.AuditUserResolver;
import ecommerce_app.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockMapper {
  private final ModelMapper modelMapper;
  private final AuditUserResolver auditUserResolver;

  public StockResponse toStockResponse(Stock stock) {
    final var stockResponse = modelMapper.map(stock, StockResponse.class);
    stockResponse.setCreatedBy(auditUserResolver.resolve(stock.getCreatedBy()));
    stockResponse.setUpdatedBy(auditUserResolver.resolve(stock.getUpdatedBy()));
    stockResponse.setProduct(ProductMapper.toProductResponse(stock.getProduct()));
    return stockResponse;
  }
}
