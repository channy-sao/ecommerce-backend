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
@Schema(description = "Base authentication response containing token details")
public class BaseAuthResponse {

  @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String accessToken;

  @Schema(
      description = "JWT refresh token used to obtain a new access token",
      example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
  private String refreshToken;

  @Schema(description = "Token type, typically Bearer", example = "Bearer")
  private String tokenType;

  @Schema(description = "Access token expiration duration in milliseconds", example = "900000")
  private long accessTokenExpireInMs;

  @Schema(description = "Refresh token expiration duration in milliseconds", example = "604800000")
  private long refreshTokenExpireInMs;
}
