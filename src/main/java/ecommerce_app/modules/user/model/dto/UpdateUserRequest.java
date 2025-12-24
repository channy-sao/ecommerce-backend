package ecommerce_app.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "UserRequest", description = "Request object for user registration or update")
public class UpdateUserRequest {

  @Schema(description = "User email address", example = "user@example.com")
  private String email;

  @Schema(description = "User password", example = "securePassword123")
  private String password;

  @Schema(description = "First name of the user", example = "John")
  private String firstName;

  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;

  @Schema(description = "Phone number", example = "+85512345678")
  private String phone;

  @Schema(description = "MultipartFile")
  private MultipartFile profile;
}
