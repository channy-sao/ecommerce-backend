package ecommerce_app.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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

  @NotNull(message = "Role name could be not null or empty")
  @Schema(description = "Name of the role", example = "Admin")
  private String roleName;

  @Schema(description = "Description of the role", example = "Administrator role with full access")
  private String description;

  @Schema(description = "Set of permission IDs to assign to the role", example = "[1, 2, 3]")
  private Set<Long> permissionIds;
}
