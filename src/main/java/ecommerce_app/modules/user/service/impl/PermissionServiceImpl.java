package ecommerce_app.modules.user.service.impl;

import ecommerce_app.modules.user.model.entity.Permission;
import ecommerce_app.modules.user.repository.PermissionRepository;
import ecommerce_app.modules.user.service.PermissionService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {
  private final PermissionRepository permissionRepository;

  @Override
  public Set<Permission> getAllPermissions() {
    log.info("Get All Permission");
    return new HashSet<>(permissionRepository.findAll());
  }
}
