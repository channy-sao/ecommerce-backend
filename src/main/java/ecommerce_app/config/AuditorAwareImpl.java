package ecommerce_app.config;

import ecommerce_app.modules.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * AuditorAware implementation used to automatically capture the ID of the currently authenticated
 * user for auditing fields such as {@code createdBy} and {@code updatedBy}.
 */
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<Long> {
  private final UserRepository userRepository;

  /**
   * Retrieves the ID of the currently authenticated user to be used as the auditor.
   *
   * @return an {@link Optional} containing the user ID if available, or empty if not authenticated
   */
  @Override
  public Optional<Long> getCurrentAuditor() {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    if (securityContext.getAuthentication() == null
        || !securityContext.getAuthentication().isAuthenticated()) {
      return Optional.empty();
    }

    Object principal = securityContext.getAuthentication().getPrincipal();

    if (principal instanceof UserDetails userDetails) {
      var user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
      return user == null ? Optional.empty() : Optional.of(user.getId());
    }

    return Optional.empty();
  }
}
