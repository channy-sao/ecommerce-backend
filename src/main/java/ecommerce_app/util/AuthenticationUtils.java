package ecommerce_app.util;

import ecommerce_app.modules.auth.custom.AuthUser;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.user.model.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticationUtils {
  public static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  public static User getCurrentUser() {
    return (User) getAuthentication().getPrincipal();
  }

  public static String getCurrentUserEmail() {
    return getCurrentUser().getEmail();
  }

  public static String getToken() {
    if (getAuthentication() instanceof JwtAuthenticationToken authenticationToken) {
      return authenticationToken.getToken().getTokenValue();
    }
    return null;
  }
}
