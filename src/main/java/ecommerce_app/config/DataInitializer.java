package ecommerce_app.config;

import ecommerce_app.constant.enums.AuthProvider;
import ecommerce_app.constant.enums.PermissionEnum;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.user.model.entity.Permission;
import ecommerce_app.modules.user.model.entity.Role;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.PermissionRepository;
import ecommerce_app.modules.user.repository.RoleRepository;
import ecommerce_app.modules.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(value = 1)
public class DataInitializer implements ApplicationRunner {
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    seedPermissions();
    seedSuperAdminRole();
    initSuperAdmin();
  }

  private void seedPermissions() {

    try {
      for (var permissionEnum : PermissionEnum.values()) {
        permissionRepository
            .findByName(permissionEnum)
            .orElseGet(
                () ->
                    permissionRepository.save(
                        Permission.builder()
                            .name(permissionEnum)
                            .category(permissionEnum.getCategory())
                            .description(permissionEnum.getDescription())
                            .build()));
        log.info("Permission {} has been created", permissionEnum.name());
      }
    } catch (Exception _) {
      log.info("Permissions not created");
    }
  }

  private void seedSuperAdminRole() {
    try {
      roleRepository
          .findByName(SUPER_ADMIN_ROLE)
          .ifPresentOrElse(
              d -> log.info("Role {} has been created", SUPER_ADMIN_ROLE),
              () ->
                  roleRepository.save(
                      Role.builder()
                          .name(SUPER_ADMIN_ROLE)
                          .permissions(new HashSet<>(permissionRepository.findAll()))
                              .isActive(true)
                          .uid(UUID.randomUUID().toString())
                          .build()));
    } catch (Exception _) {
      log.info("Roles not created");
    }
  }

  private void initSuperAdmin() {

    // Check if super admin already exists
    userRepository
        .findByEmail("admin@gmail.com")
        .ifPresentOrElse(
            user -> {
              log.info("Admin already exists");
              return;
            },
            () -> {
              Role role =
                  roleRepository
                      .findByName(DataInitializer.SUPER_ADMIN_ROLE)
                      .orElseThrow(
                          () -> new ResourceNotFoundException("Super admin role is not found"));
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
                      .authProvider(AuthProvider.LOCAL)
                      .rememberMe(true)
                      .emailVerifiedAt(LocalDateTime.now())
                      .roles(Set.of(role))
                      .build();

              userRepository.save(admin);

              log.info("Super Admin initialized successfully!");
            });
  }
}
