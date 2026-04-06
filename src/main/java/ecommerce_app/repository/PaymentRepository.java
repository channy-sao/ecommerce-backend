package ecommerce_app.repository;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Optional<Payment> findByGatewayReference(String gatewayReference);

  Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

  List<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);

  Optional<Payment> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);

  Optional<Payment> findByOrderId(Long orderId);

  List<Payment> findByGatewayAndStatusAndExpiredAtBefore(
      PaymentGateway gateway, PaymentStatus status, LocalDateTime time);

  @Query(
      "SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.gateway = :gateway ORDER BY p.createdAt DESC")
  List<Payment> findByOrderIdAndGateway(
      @Param("orderId") Long orderId, @Param("gateway") PaymentGateway gateway);
}
