package ecommerce_app.util;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.model.response.AuditUserDto;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

  // prevent too much call to repository for performance
  public Map<Long, AuditUserDto> resolve(Collection<Long> userIds) {
    if (userIds == null) {
      return Collections.emptyMap();
    }
    final var users = userRepository.findAllById(userIds);
    return users.stream()
        .map(user -> new AuditUserDto(user.getId(), user.getFullName()))
        .collect(Collectors.toMap(AuditUserDto::id, Function.identity()));
  }
}
