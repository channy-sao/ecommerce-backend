package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO to create a new role with permissions")
public class CreateRoleRequest {

  @NotBlank(message = "Role name is required")
  @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
  @Pattern(
      regexp = "^[a-zA-Z][a-zA-Z0-9_\\s]*$",
      message =
          "Role name must start with a letter and contain only letters, digits, underscores, or spaces")
  @Schema(description = "Name of the role", example = "SUPER_ADMIN")
  private String roleName;

  @Size(max = 255, message = "Description must not exceed 255 characters")
  @Schema(description = "Description of the role", example = "Administrator role with full access")
  private String description;

  @NotEmpty(message = "At least one permission is required")
  @Schema(description = "Set of permission IDs to assign to the role", example = "[1, 2, 3]")
  private Set<Long> permissionIds;
}
