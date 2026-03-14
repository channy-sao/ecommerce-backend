package ecommerce_app.dto.response;

import ecommerce_app.entity.Permission;
import ecommerce_app.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
  private Set<SimplePermissionResponse> permissions;

  @Schema(description = "Status Role", example = "Status of role")
  private boolean isActive;

  public static RoleResponse toRoleResponse(final Role role) {
    final var roleResponse = new RoleResponse();
    roleResponse.setId(role.getId());
    roleResponse.setName(role.getName());
    roleResponse.setDescription(role.getDescription());
    roleResponse.setPermissions(toSimplePermissionResponse(role.getPermissions()));
    roleResponse.setActive(role.isActive());
    return roleResponse;
  }

  public static Set<SimplePermissionResponse> toSimplePermissionResponse(
      final Set<Permission> permissions) {
    return permissions.stream()
        .map(SimplePermissionResponse::toSimplePermissionResponse)
        .collect(Collectors.toSet());
  }
}
