package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
  @NotBlank
  private String resetToken;

  @NotBlank
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String newPassword;

  @NotBlank private String confirmPassword;
}
