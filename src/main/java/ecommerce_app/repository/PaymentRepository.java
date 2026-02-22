package ecommerce_app.repository;

import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.entity.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Optional<Payment> findByGatewayReference(String gatewayReference);

  Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

  List<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);

  Optional<Payment> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);
}
