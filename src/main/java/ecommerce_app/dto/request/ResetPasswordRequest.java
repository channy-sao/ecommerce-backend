package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "ResetPasswordRequest", description = "Request object for resetting password")
public class ResetPasswordRequest {

  @NotBlank(message = "Reset token is required")
  @Schema(
      description = "Password reset token received via email",
      example = "eyJhbGciOiJSUzI1NiJ9...")
  private String resetToken;

  @NotBlank(message = "New password is required")
  @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
  @Schema(description = "New password", example = "Secure@123")
  private String newPassword;

  @NotBlank(message = "Confirm password is required")
  @Schema(description = "Must match new password", example = "Secure@123")
  private String confirmPassword;
}
