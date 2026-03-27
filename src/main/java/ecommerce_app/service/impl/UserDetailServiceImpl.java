package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.AuthProvider;
import ecommerce_app.core.identify.custom.AuthUser;
import ecommerce_app.core.identify.custom.AuthUserLoader;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.entity.Role;
import ecommerce_app.entity.User;
import ecommerce_app.repository.UserRepository;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
            .findByEmailAndIsActive(username, true)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    if (!Objects.equals(user.getAuthProvider(), AuthProvider.LOCAL)) {
      log.warn("User {} provider is not LOCAL", user.getEmail());
    }

    return buildUserDetails(user);
  }

  @Transactional(readOnly = true)
  public UserDetails loadUserById(Long id) {
    User user = userRepository
            .findByIdAndIsActive(id, true)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
    return buildUserDetails(user);
  }

  private UserDetails buildUserDetails(User user) {
    Set<GrantedAuthority> authorities = buildAuthorities(user);

    AuthUser authUser = AuthUser.builder()
            .id(user.getId())
            .email(user.getEmail())
            .password(user.getPassword())
            .enabled(user.getIsActive())
            .authorities(authorities)
            .build();

    return new CustomUserDetails(authUser);
  }

  private Set<GrantedAuthority> buildAuthorities(User user) {
    Set<GrantedAuthority> authorities = new HashSet<>();

    if (!CollectionUtils.isEmpty(user.getRoles())) {
      for (Role role : user.getRoles()) {
        // Add role
        authorities.add(new SimpleGrantedAuthority(AuthUserLoader.ROLE_PREFIX + role.getName()));

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
