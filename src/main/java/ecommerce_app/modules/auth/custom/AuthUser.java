package ecommerce_app.modules.auth.custom;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@Builder
public class AuthUser {
  private Long id;
  private String email;
  private String password;
  private Set<String> authorities;
  private boolean enabled;
}
