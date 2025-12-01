package ecommerce_app.modules.auth.service.impl;

import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    if (!Objects.equals(user.getProvider(), "LOCAL")) {
//      throw new UsernameNotFoundException("User not registered for local login");
        log.warn("User {} provider is not LOCAL", user.getEmail());
    }
    return new CustomUserDetails(user);
  }
}
