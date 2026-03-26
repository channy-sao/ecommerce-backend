package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Login response containing auth tokens and user information")
public class LoginResponse extends BaseAuthResponse {

  @Schema(description = "Authenticated user details")
  private UserResponse userInfo;
}
