package ecommerce_app.modules.stock.repository;

import ecommerce_app.modules.stock.model.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock getByProductId(Long productId);

    Optional<Stock> findByProductId(Long productId);
}
