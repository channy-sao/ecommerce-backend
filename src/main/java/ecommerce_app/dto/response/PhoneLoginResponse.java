package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Phone login response extending login response with profile completion status")
public class PhoneLoginResponse extends LoginResponse {

  @Schema(
      description =
          "Indicates whether the user's profile is incomplete. "
              + "If true, frontend should show the 'complete your profile' screen.",
      example = "false")
  private boolean profileIncomplete;
}
