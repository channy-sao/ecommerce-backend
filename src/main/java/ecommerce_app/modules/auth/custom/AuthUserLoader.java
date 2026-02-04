package ecommerce_app.modules.auth.custom;

import ecommerce_app.modules.user.model.entity.Role;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class AuthUserLoader {

  public static final String ROLE_PREFIX = "ROLE_";
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public CustomUserDetails loadByEmail(String email) {

    final User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    Set<GrantedAuthority> authorities = buildAuthorities(user);

    AuthUser authUser =
        AuthUser.builder()
            .id(user.getId())
            .email(user.getEmail())
            .password(user.getPassword())
            .enabled(user.getIsActive())
            .authorities(authorities)
            .build();

    return CustomUserDetails.builder().user(authUser).build();
  }

  private Set<GrantedAuthority> buildAuthorities(User user) {
    Set<GrantedAuthority> authorities = new HashSet<>();

    if (!CollectionUtils.isEmpty(user.getRoles())) {
      for (Role role : user.getRoles()) {
        // Add role
        authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role.getName()));

        // Add permissions
        if (role.getPermissions() != null) {
          role.getPermissions()
              .forEach(
                  permission ->
                      authorities.add(
                          new SimpleGrantedAuthority(String.valueOf(permission.getName()))));
        }
      }
    }

    return authorities;
  }
}
