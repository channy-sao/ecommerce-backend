package ecommerce_app.repository;

import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.entity.KHQRPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface KHQRPaymentRepository extends JpaRepository<KHQRPayment, Long> {
  Optional<KHQRPayment> findByOrderId(Long orderId);

  Optional<KHQRPayment> findByBakongTransactionId(String transactionId);

  List<KHQRPayment> findByStatusAndExpiresAtBefore(PaymentStatus status, Instant dateTime);
}
