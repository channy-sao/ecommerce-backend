package ecommerce_app.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update password request DTO for changing user password")
public class UpdatePasswordRequest {

  @NotBlank(message = "Old password is required")
  @Schema(
      description = "User's current password for verification",
      example = "OldPassword@123",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String oldPassword;

  @NotBlank(message = "New password is required")
  @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
  @Schema(
      description = "New password to set for the user",
      example = "NewSecurePassword@456",
      requiredMode = Schema.RequiredMode.REQUIRED,
      minLength = 8,
      maxLength = 100)
  private String newPassword;

  @NotBlank(message = "Confirm new password is required")
  @Schema(
      description = "Confirmation of the new password (must match newPassword)",
      example = "NewSecurePassword@456",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String confirmNewPassword;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  @Schema(
      description = "User's email address for verification",
      example = "user@example.com",
      requiredMode = Schema.RequiredMode.REQUIRED,
      format = "email")
  private String email;
}
