package ecommerce_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneLoginResponse extends LoginResponse {

  /**
   * true → new user, frontend should show the "complete your profile" screen false → existing user,
   * proceed normally
   */
  private boolean profileIncomplete;
}
