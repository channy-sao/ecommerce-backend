package ecommerce_app.modules.user.model.dto;

import ecommerce_app.modules.user.model.entity.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
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

  @Schema(description = "First name of the user", example = "John")
  private String firstName;

  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;

  @Schema(description = "Full Name of the user", example = "John Doe")
  private String fullName;

  @Schema(description = "User phone number", example = "+85512345678")
  private String phone;

  @Schema(
      description = "URL to the user's avatar image",
      example = "https://example.com/images/avatar.png")
  private String avatar;

  @Schema(description = "Status of the user account", example = "true")
  private Boolean isActive;

  @Schema(description = "Email Verified of the user account", example = "true")
  private Boolean isEmailVerified;

  @Schema(description = "Email Verified Time of the user account", example = "true")
  private LocalDateTime emailVerifiedAt;

  @Schema(description = "Created date time of user")
  private LocalDateTime createdAt;

  @Schema(description = "Updated date time of user")
  private LocalDateTime updatedAt;

  @Schema(description = "Role representation of user")
  private Set<RoleResponse> roles;

  @Schema(description = "Permission of user")
  private Set<Permission> permissions;
}
