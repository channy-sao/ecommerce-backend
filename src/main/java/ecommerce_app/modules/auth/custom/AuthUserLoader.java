package ecommerce_app.modules.auth.custom;

import ecommerce_app.constant.enums.AuthProvider;
import ecommerce_app.modules.user.model.entity.Role;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class AuthUserLoader {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public CustomUserDetails loadByEmail(String email) {

    final User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    final Set<String> authorities = new HashSet<>();

    if (!CollectionUtils.isEmpty(user.getRoles())) {
      for (Role role : user.getRoles()) {
        authorities.add("ROLE_" + role.getName());
        if (role.getPermissions() != null) {
          role.getPermissions().forEach(p -> authorities.add(String.valueOf(p.getName())));
        }
      }
    }

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
}
