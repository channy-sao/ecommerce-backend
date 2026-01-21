package ecommerce_app.config;

import ecommerce_app.modules.auth.custom.CustomUserDetails;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * AuditorAware implementation used to automatically capture the ID of the currently authenticated
 * user for auditing fields such as {@code createdBy} and {@code updatedBy}.
 */
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<Long> {

  /**
   * Retrieves the ID of the currently authenticated user to be used as the auditor.
   *
   * @return an {@link Optional} containing the user ID if available, or empty if not authenticated
   */
  @Override
  public Optional<Long> getCurrentAuditor() {
    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      return Optional.empty();
    }

    Object principal = auth.getPrincipal();

    // If principal is your custom UserDetails
    if (principal instanceof CustomUserDetails(ecommerce_app.modules.auth.custom.AuthUser user)) {
      return Optional.of(user.getId()); // <- safe, no repository call
    }

    return Optional.empty();
  }
}
