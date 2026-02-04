package ecommerce_app.util;

import ecommerce_app.modules.auth.custom.CustomUserDetails;
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

  public static CustomUserDetails getCurrentUser() {
    return (CustomUserDetails) getAuthentication().getPrincipal();
  }

  public static Long getCurrentUserId() {
    var authUser = (CustomUserDetails) getAuthentication().getPrincipal();
    if (authUser != null) {
      return authUser.getId();
    }
    return null;
  }

  public static String getCurrentUserEmail() {
    return getCurrentUser().getUsername();
  }

  public static String getToken() {
    if (getAuthentication() instanceof JwtAuthenticationToken authenticationToken) {
      return authenticationToken.getToken().getTokenValue();
    }
    return null;
  }
}
