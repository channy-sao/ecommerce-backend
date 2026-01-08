package ecommerce_app.modules.auth.service.impl;

import ecommerce_app.modules.auth.custom.AuthUser;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.user.model.entity.Role;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    if (!Objects.equals(user.getProvider(), "LOCAL")) {
      // throw new UsernameNotFoundException("User not registered for local login");
      log.warn("User {} provider is not LOCAL", user.getEmail());
    }

    Set<String> authorities = new HashSet<>();

    for (Role role : user.getRoles()) {
      authorities.add("ROLE_" + role.getName());
      role.getPermissions().forEach(p -> authorities.add(String.valueOf(p.getName())));
    }

    AuthUser authUser =
        AuthUser.builder()
            .id(user.getId())
            .email(user.getEmail())
            .password(user.getPassword())
            .enabled(user.getIsActive())
            .authorities(authorities)
            .build();
    return new CustomUserDetails(authUser);
  }
}
