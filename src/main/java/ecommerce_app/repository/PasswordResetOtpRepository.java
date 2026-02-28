package ecommerce_app.repository;

import ecommerce_app.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

  Optional<PasswordResetOtp> findTopByEmailOrderByCreatedAtDesc(String email);

  Optional<PasswordResetOtp> findByResetToken(String resetToken);

  void deleteAllByEmail(String email);

  // For cleanup scheduler
  void deleteAllByExpiresAtBefore(LocalDateTime now);
}
