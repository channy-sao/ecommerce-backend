package ecommerce_app.util;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.model.response.AuditUserDto;
import ecommerce_app.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditUserResolver {
  private final UserRepository userRepository;

  public AuditUserDto resolve(Long userId) {
    if (userId == null) {
      return null;
    }
    final var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Audit User not found"));
    return new AuditUserDto(userId, user.getFullName());
  }
}
