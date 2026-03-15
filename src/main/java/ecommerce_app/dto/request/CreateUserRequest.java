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
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CreateUserRequest", description = "Request object for user registration or update")
public class CreateUserRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  @Schema(description = "User email address", example = "user@example.com")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, max = 100, message = "Password must be between 8 and 100 characters")
  @Schema(description = "User password", example = "securePassword123")
  private String password;

  @NotBlank(message = "First name is required")
  @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
  @Pattern(
      regexp = "^[a-zA-Z\\s'-]+$",
      message = "First name must contain only letters, spaces, hyphens, or apostrophes")
  @Schema(description = "First name of the user", example = "John")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
  @Pattern(
      regexp = "^[a-zA-Z\\s'-]+$",
      message = "Last name must contain only letters, spaces, hyphens, or apostrophes")
  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;

  @Pattern(
      regexp = "^\\+?[0-9]{7,15}$",
      message = "Phone number must be between 7 and 15 digits and may start with +")
  @Schema(description = "Phone number", example = "+85512345678")
  private String phone;

  @Schema(description = "MultipartFile")
  private MultipartFile profile;
}
