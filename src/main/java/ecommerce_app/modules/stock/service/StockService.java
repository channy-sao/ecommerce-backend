package ecommerce_app.modules.stock.service;

import ecommerce_app.modules.stock.model.dto.StockResponse;

import java.util.List;

public interface StockService {
    StockResponse getByProductId(Long productId);

    void increaseStock(Long productId, int quantity);

    void adjustStock(Long productId, int quantity);

    List<StockResponse> getStocks();
}
