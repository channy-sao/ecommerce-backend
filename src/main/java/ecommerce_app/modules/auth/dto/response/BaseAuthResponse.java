package ecommerce_app.modules.auth.dto.response;

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
public class BaseAuthResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private long accessTokenExpireInMs;
  private long refreshTokenExpireInMs;
}
