package ecommerce_app.scheduler;

import ecommerce_app.repository.PasswordResetOtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupScheduler {

  private final PasswordResetOtpRepository otpRepository;

  // Run every hour — delete expired OTPs
  @Scheduled(fixedRate = 60 * 60 * 1000)
  @Transactional
  public void cleanupExpiredOtps() {
    otpRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    log.info("Cleaned up expired OTPs");
  }
}
