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
@Schema(description = "Request DTO to update an existing role and its permissions")
public class UpdateRoleRequest {

  @NotNull(message = "New name must be not null or empty")
  @Schema(description = "New name of the role", example = "Manager")
  private String roleName;

  @Schema(description = "New description of the role", example = "Manager role with limited access")
  private String description;

  @Schema(
      description = "Updated set of permission IDs to assign to the role",
      example = "[1, 2, 4]")
  private Set<Long> permissionIds;
}
