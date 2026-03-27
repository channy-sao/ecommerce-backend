package ecommerce_app.core.identify.custom;

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
  private Set<GrantedAuthority> authorities;
  private boolean enabled;
  private String fullName;
}
