package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(name = "RefreshTokenRequest", description = "Request object for refreshing access token")
public class RefreshTokenRequest {

  @NotBlank(message = "Refresh token is required")
  @Schema(description = "JWT refresh token", example = "eyJhbGciOiJSUzI1NiJ9...")
  private String refreshToken;
}
