package ecommerce_app.modules.user.model.dto;

import ecommerce_app.modules.user.model.entity.Permission;
import ecommerce_app.modules.user.model.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role response object")
public class RoleResponse {

  @Schema(description = "Role unique identifier", example = "1")
  private Long id;

  @Schema(description = "Role name", example = "ADMIN")
  private String name;

  @Schema(description = "Role description", example = "System administrator role")
  private String description;

  @Schema(description = "Permissions assigned to the role")
  private Set<Permission> permissions;

  @Schema(description = "Status Role", example = "Status of role")
  private boolean isActive;

  public static RoleResponse toRoleResponse(final Role role) {
    final var roleResponse = new RoleResponse();
    roleResponse.setId(role.getId());
    roleResponse.setName(role.getName());
    roleResponse.setDescription(role.getDescription());
    roleResponse.setPermissions(role.getPermissions());
    roleResponse.setActive(role.isActive());
    return roleResponse;
  }
}
