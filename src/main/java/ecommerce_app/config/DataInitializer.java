package ecommerce_app.config;

import ecommerce_app.constant.enums.PermissionEnum;
import ecommerce_app.modules.user.model.entity.Permission;
import ecommerce_app.modules.user.model.entity.Role;
import ecommerce_app.modules.user.repository.PermissionRepository;
import ecommerce_app.modules.user.repository.RoleRepository;
import java.util.HashSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(value = 1)
public class DataInitializer implements ApplicationRunner {
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
  public static final String ADMIN_ROLE = "ADMIN";

  @Override
  public void run(ApplicationArguments args) throws Exception {
    seedPermissions();
    seedSuperAdminRole();
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
    } catch (Exception e) {
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
                          .uid(UUID.randomUUID().toString())
                          .build()));
    } catch (Exception e) {
      log.info("Roles not created");
    }
  }
}
