package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotEmpty;
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
public class AssignRoleToUserRequest {

  @NotEmpty(message = "Role IDs must not be empty")
  private Set<Long> roleIds;
}
