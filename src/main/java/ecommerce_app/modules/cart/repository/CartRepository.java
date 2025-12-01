package ecommerce_app.modules.cart.repository;

import ecommerce_app.constant.enums.CartStatus;
import ecommerce_app.modules.cart.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>, JpaSpecificationExecutor<Cart> {
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);
    List<Cart> findByUserId(Long userId);
}
