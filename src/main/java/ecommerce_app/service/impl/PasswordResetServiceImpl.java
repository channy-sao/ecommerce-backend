package ecommerce_app.service.impl;

import ecommerce_app.dto.request.ResetPasswordRequest;
import ecommerce_app.dto.request.VerifyOtpRequest;
import ecommerce_app.dto.response.VerifyOtpResponse;
import ecommerce_app.entity.PasswordResetOtp;
import ecommerce_app.entity.User;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.InternalServerErrorException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.repository.PasswordResetOtpRepository;
import ecommerce_app.repository.UserRepository;
import ecommerce_app.service.EmailService;
import ecommerce_app.service.PasswordResetService;
import ecommerce_app.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

  private final PasswordResetOtpRepository otpRepository;
  private final UserRepository userRepository;
  private final EmailService emailService;
  private final PasswordEncoder passwordEncoder;
  private final OtpUtil otpUtil;

  private static final int OTP_EXPIRY_MINUTES = 5;
  private static final int RESET_TOKEN_EXPIRY_MINUTES = 10;
  private static final int MAX_ATTEMPTS = 5;
  private static final int RESEND_COOLDOWN_SECONDS = 60;

  // ── Step 1: Send OTP ─────────────────────────────────────────────────────

  @Override
  public void sendOtp(String email) {
    // Always respond the same — don't reveal if email exists
    userRepository
        .findByEmail(email)
        .ifPresent(
            user -> {

              // Cooldown check — prevent spam
              otpRepository
                  .findTopByEmailOrderByCreatedAtDesc(email)
                  .ifPresent(
                      existing -> {
                        long secondsSinceLastSent =
                            ChronoUnit.SECONDS.between(
                                existing.getCreatedAt(), LocalDateTime.now());
                        if (secondsSinceLastSent < RESEND_COOLDOWN_SECONDS) {
                          throw new BadRequestException(
                              "Please wait "
                                  + (RESEND_COOLDOWN_SECONDS - secondsSinceLastSent)
                                  + " seconds before requesting a new code");
                        }
                      });

              // Delete old OTPs for this email
              otpRepository.deleteAllByEmail(email);

              // Generate and hash OTP
              String otp = otpUtil.generateOtp();
              String otpHash = otpUtil.hashOtp(otp);

              // Save
              PasswordResetOtp resetOtp =
                  PasswordResetOtp.builder()
                      .email(email)
                      .otpHash(otpHash)
                      .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                      .otpVerified(false)
                      .used(false)
                      .attempts(0)
                      .build();
              otpRepository.save(resetOtp);
              log.info("OTP Code : {}", otp );

              // Send email
              emailService.sendOtpEmail(email, user.getFullName(), otp);

              log.info("OTP sent to email: {}", email);
            });
  }

  // ── Step 2: Verify OTP ───────────────────────────────────────────────────

  @Override
  public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
    PasswordResetOtp resetOtp =
        otpRepository
            .findTopByEmailOrderByCreatedAtDesc(request.getEmail())
            .orElseThrow(() -> new BadRequestException("No OTP request found for this email"));

    // Check already used
    if (Boolean.TRUE.equals(resetOtp.getUsed())) {
      throw new BadRequestException("This OTP has already been used");
    }

    // Check already verified (somehow called twice)
    if (Boolean.TRUE.equals(resetOtp.getOtpVerified())) {
      throw new BadRequestException("OTP already verified. Proceed to reset password");
    }

    // Check max attempts — brute force protection
    if (resetOtp.getAttempts() >= MAX_ATTEMPTS) {
      otpRepository.delete(resetOtp); // invalidate
      throw new BadRequestException("Too many failed attempts. Please request a new OTP");
    }

    // Check expiry
    if (resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BadRequestException("OTP has expired. Please request a new one");
    }

    // Verify OTP
    if (!otpUtil.verifyOtp(request.getOtp(), resetOtp.getOtpHash())) {
      resetOtp.setAttempts(resetOtp.getAttempts() + 1);
      otpRepository.save(resetOtp);

      int remaining = MAX_ATTEMPTS - resetOtp.getAttempts();
      throw new BadRequestException("Invalid OTP. " + remaining + " attempt(s) remaining");
    }

    // OTP is valid — generate reset token
    String resetToken = UUID.randomUUID().toString();
    resetOtp.setOtpVerified(true);
    resetOtp.setResetToken(resetToken);
    resetOtp.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
    otpRepository.save(resetOtp);

    log.info("OTP verified for email: {}", request.getEmail());

    return VerifyOtpResponse.builder()
        .resetToken(resetToken)
        .message("OTP verified. You can now reset your password")
        .build();
  }

  // ── Step 3: Reset Password ───────────────────────────────────────────────

  @Override
  public void resetPassword(ResetPasswordRequest request) {
    // Validate passwords match
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new BadRequestException("Passwords do not match");
    }

    // Find by reset token
    PasswordResetOtp resetOtp =
        otpRepository
            .findByResetToken(request.getResetToken())
            .orElseThrow(() -> new BadRequestException("Invalid or expired reset session"));

    // Must be OTP-verified
    if (Boolean.FALSE.equals(resetOtp.getOtpVerified())) {
      throw new BadRequestException("OTP not verified");
    }

    // Check used
    if (Boolean.TRUE.equals(resetOtp.getUsed())) {
      throw new BadRequestException("This reset session has already been used");
    }

    // Check reset token expiry
    if (resetOtp.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
      throw new InternalServerErrorException("Reset session expired. Please start over");
    }

    // Find user and update password
    User user =
        userRepository
            .findByEmail(resetOtp.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Invalidate OTP record
    resetOtp.setUsed(true);
    otpRepository.save(resetOtp);

    log.info("Password reset successfully for: {}", resetOtp.getEmail());
  }
}
