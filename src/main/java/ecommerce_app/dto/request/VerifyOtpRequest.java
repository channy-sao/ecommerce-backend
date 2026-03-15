package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "VerifyOtpRequest", description = "Request object for verifying OTP")
public class VerifyOtpRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  @Schema(description = "Email address that received the OTP", example = "user@example.com")
  private String email;

  @NotBlank(message = "OTP is required")
  @Size(min = 6, max = 6, message = "OTP must be exactly 6 characters")
  @Pattern(regexp = "^[0-9]{6}$", message = "OTP must contain only digits")
  @Schema(description = "6-digit OTP code", example = "123456")
  private String otp;
}
