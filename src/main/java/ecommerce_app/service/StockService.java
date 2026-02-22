package ecommerce_app.service;

import ecommerce_app.constant.enums.StockStatus;
import ecommerce_app.dto.response.StockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface StockService {
    StockResponse getByProductId(Long productId);

    void increaseStock(Long productId, int quantity);

    void adjustStock(Long productId, int quantity);

    Page<StockResponse> getStocks(boolean isPaged, int page, int pageSize, String sortBy, Sort.Direction sortDirection, String filter, StockStatus stockStatus);
}
