package ecommerce_app.core.identify.custom;

import java.util.Collection;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Builder
public record CustomUserDetails(AuthUser user) implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {

    return user.getAuthorities();
  }

  public Long getId() {
    return user.getId();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isEnabled() {
    return user.isEnabled();
  }

  public String getFullName() { // 👈 add this
    return user.getFullName();
  }
}
