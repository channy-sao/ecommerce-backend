package ecommerce_app.modules.auth.custom;

import java.util.Collection;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Builder
public record CustomUserDetails(AuthUser user) implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {

    return user.getAuthorities().stream().map(SimpleGrantedAuthority::new).toList();
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
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
