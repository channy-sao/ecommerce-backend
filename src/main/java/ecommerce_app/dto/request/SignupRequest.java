package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
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
@Schema(name = "SignupRequest", description = "Request object for user signup via social login")
public class SignupRequest {

  @NotBlank(message = "ID token is required")
  @Schema(description = "Firebase or OAuth ID token", example = "eyJhbGciOiJSUzI1NiJ9...")
  private String idToken;

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
}
