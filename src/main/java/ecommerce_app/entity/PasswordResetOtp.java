package ecommerce_app.entity;

import ecommerce_app.entity.base.TimeAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "password_reset_otps",
    indexes = {
      @Index(name = "idx_otp_email", columnList = "email"),
      @Index(name = "idx_otp_token", columnList = "reset_token"),
      @Index(name = "idx_otp_expires", columnList = "expires_at")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetOtp extends TimeAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String otpHash; // hashed OTP — never store plain

  @Column(name = "reset_token", unique = true)
  private String resetToken; // issued after OTP verified, used to reset password

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "reset_token_expires_at")
  private LocalDateTime resetTokenExpiresAt;

  @Column(nullable = false)
  private Boolean otpVerified = false;

  @Column(nullable = false)
  private Boolean used = false;

  @Column(nullable = false)
  private Integer attempts = 0; // brute force protection
}
