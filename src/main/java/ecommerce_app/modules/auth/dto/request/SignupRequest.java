package ecommerce_app.modules.auth.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class SignupRequest {
  @NotNull(message = "idToken is required")
  private String idToken;

  @NotNull(message = "First Name is required")
  private String firstName;

  @NotNull(message = "Last Name is required")
  private String lastName;
}
