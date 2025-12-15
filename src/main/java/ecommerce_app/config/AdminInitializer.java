package ecommerce_app.config;

import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminInitializer {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @EventListener(ApplicationReadyEvent.class)
  public void initSuperAdmin() {

    // Check if super admin already exists
    userRepository
        .findByEmail("admin@gmail.com")
        .ifPresentOrElse(
            user -> {
              log.info("Admin already exists");
              return;
            },
            () -> {
              // Create super admin user
              User admin =
                  User.builder()
                      .email("admin@gmail.com")
                      .password(passwordEncoder.encode("admin@123"))
                      .avatar("admin-avatar.png")
                      .firstName("Admin")
                      .lastName("Admin")
                      .phone("+855 356789")
                      .uuid(UUID.randomUUID())
                      .isActive(true)
                      .provider("LOCAL")
                      .rememberMe(true)
                      .emailVerifiedAt(LocalDateTime.now())
                      .build();

              userRepository.save(admin);

              log.info("Super Admin initialized successfully!");
            });
  }
}
