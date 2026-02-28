package ecommerce_app.util;

import ecommerce_app.exception.BadRequestException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OtpUtil {

  private static final int OTP_LENGTH = 6;

  // Generate 6-digit OTP
  public String generateOtp() {
    SecureRandom random = new SecureRandom();
    int otp = 100000 + random.nextInt(900000); // always 6 digits
    return String.valueOf(otp);
  }

  // Hash OTP using SHA-256 — lightweight, fine for short-lived OTPs
  public String hashOtp(String otp) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
        log.error(e.getMessage(), e);
      throw new BadRequestException("Failed to hash OTP");
    }
  }

  // Verify OTP against its hash
  public boolean verifyOtp(String otp, String hash) {
    return hashOtp(otp).equals(hash);
  }
}
