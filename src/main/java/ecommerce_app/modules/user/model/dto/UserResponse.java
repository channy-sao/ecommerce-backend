package ecommerce_app.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "UserResponse", description = "Response DTO for user information")
public class UserResponse {

  @Schema(description = "User ID", example = "1")
  private Long id;

  @Schema(description = "User email address", example = "user@example.com")
  private String email;

  @Schema(description = "User password (should not be exposed in real APIs)", example = "encryptedPassword")
  private String password;

  @Schema(description = "First name of the user", example = "John")
  private String firstName;

  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;

  @Schema(description = "User phone number", example = "+85512345678")
  private String phone;

  @Schema(description = "URL to the user's avatar image", example = "https://example.com/images/avatar.png")
  private String avatar;

  @Schema(description = "Status of the user account", example = "true")
  private Boolean isActive;
}
