package ecommerce_app.modules.cart.repository;

import ecommerce_app.modules.cart.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository
    extends JpaRepository<CartItem, Long>, JpaSpecificationExecutor<CartItem> {}
