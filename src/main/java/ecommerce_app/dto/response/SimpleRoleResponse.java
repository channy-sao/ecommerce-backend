package ecommerce_app.dto.response;

import ecommerce_app.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Schema(description = "Simple role response")
public class SimpleRoleResponse {

  @Schema(description = "Role unique identifier", example = "1")
  private Long id;

  @Schema(description = "Role name", example = "ADMIN")
  private String name;

  public static SimpleRoleResponse roleResponse(Role role) {
    return SimpleRoleResponse.builder().id(role.getId()).name(role.getName()).build();
  }
}
