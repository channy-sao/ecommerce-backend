package ecommerce_app.config;

import ecommerce_app.modules.user.model.entity.Permission;
import ecommerce_app.modules.user.model.entity.Role;
import ecommerce_app.modules.user.repository.PermissionRepository;
import ecommerce_app.modules.user.repository.RoleRepository;
import java.util.List;
import java.util.Set;
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

  @Override
  public void run(ApplicationArguments args) throws Exception {
    seedPermissions();
    seedRoles();
  }

  private void seedPermissions() {
    List<String> permissions =
        List.of(
            "USER_READ",
            "USER_CREATE",
            "USER_UPDATE",
            "PRODUCT_READ",
            "PRODUCT_CREATE",
            "ORDER_CANCEL");

    try {
      permissions.forEach(
          p ->
              permissionRepository
                  .findByName(p)
                  .orElseGet(
                      () -> permissionRepository.save(Permission.builder().name(p).build())));
      log.info("Permissions created");
    } catch (Exception e) {
      log.info("Permissions not created");
    }
  }

  private void seedRoles() {
    try {

      Permission userRead = permissionRepository.findByName("USER_READ").orElseThrow();
      Permission userCreate = permissionRepository.findByName("USER_CREATE").orElseThrow();
      Permission productRead = permissionRepository.findByName("PRODUCT_READ").orElseThrow();
      Permission productCreate = permissionRepository.findByName("PRODUCT_CREATE").orElseThrow();

      roleRepository
          .findByName("ROLE_SUPER_ADMIN")
          .orElseGet(
              () ->
                  roleRepository.save(
                      Role.builder()
                          .name("ROLE_SUPER_ADMIN")
                          .permissions(Set.of(userRead, userCreate, productRead, productCreate))
                          .build()));

      roleRepository
          .findByName("ROLE_ADMIN")
          .orElseGet(
              () ->
                  roleRepository.save(
                      Role.builder()
                          .name("ROLE_ADMIN")
                          .permissions(Set.of(userRead, productRead, productCreate))
                          .build()));
    } catch (Exception e) {
      log.info("Roles not created");
    }
  }
}
