package ecommerce_app.modules.user.service.impl;

import ecommerce_app.infrastructure.exception.BadRequestException;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.user.model.dto.CreateRoleRequest;
import ecommerce_app.modules.user.model.dto.RoleResponse;
import ecommerce_app.modules.user.model.dto.UpdateRoleRequest;
import ecommerce_app.modules.user.model.entity.Role;
import ecommerce_app.modules.user.repository.PermissionRepository;
import ecommerce_app.modules.user.repository.RoleRepository;
import ecommerce_app.modules.user.service.RoleService;
import ecommerce_app.modules.user.service.ValidatePermission;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoleServiceImpl extends ValidatePermission implements RoleService {
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final ModelMapper modelMapper;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void createRole(CreateRoleRequest createRoleRequest) {
    log.info("Start create role.");
    this.checkExist(createRoleRequest.getRoleName(), true, null);
    this.validatePermission(createRoleRequest.getPermissionIds());
    final var permissions =
        this.permissionRepository.findAllById(createRoleRequest.getPermissionIds());
    Role role = new Role();
    role.setPermissions(new HashSet<>(permissions));
    role.setName(createRoleRequest.getRoleName());
    role.setUid(UUID.randomUUID().toString());
    role.setDescription(createRoleRequest.getDescription());
    roleRepository.save(role);
    log.info("Role created.");
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateRole(UpdateRoleRequest updateRoleRequest, long roleId) {
    log.info("Start update role.");
    roleRepository
        .findById(roleId)
        .ifPresentOrElse(
            role -> {
              this.checkExist(role.getName(), false, updateRoleRequest.getRoleName());
              validatePermission(updateRoleRequest.getPermissionIds());
              final var permissions =
                  this.permissionRepository.findAllById(updateRoleRequest.getPermissionIds());
              role.setName(updateRoleRequest.getRoleName());
              role.setPermissions(new HashSet<>(permissions));
              role.setDescription(updateRoleRequest.getDescription());
              roleRepository.save(role);
            },
            () -> {
              throw new ResourceNotFoundException("Role", roleId);
            });
  }

  @Override
  public void deleteRole(long roleId) {
    log.info("Start soft delete role id={}", roleId);

    final Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

    role.softDelete(); // ENTITY responsibility
    roleRepository.save(role);

    log.info("Role soft deleted id={}", roleId);
  }

  @Override
  public RoleResponse getRole(long roleId) {
    log.info("Start get role id={}", roleId);
    final var role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
    return modelMapper.map(role, RoleResponse.class);
  }

  @Override
  public Set<RoleResponse> getRoles() {
    log.info("Start get roles.");
    return roleRepository.findAll().stream()
        .map(role -> modelMapper.map(role, RoleResponse.class))
        .collect(Collectors.toSet());
  }

  @Transactional(readOnly = true)
  @Override
  public Set<RoleResponse> searchRole(String roleName) {
    return roleRepository
        .findByName("%" + roleName + "%")
        .map(RoleResponse::toRoleResponse)
        .stream()
        .collect(Collectors.toSet());
  }

  @Override
  protected void validatePermission(Set<Long> permissionIds) {
    if (permissionIds.isEmpty()) {
      throw new IllegalArgumentException("permission is empty");
    }
  }

  private void checkExist(String roleName, boolean isInsert, String newRoleName) {
    if (isInsert) {
      roleRepository
          .findByName(roleName)
          .ifPresent(
              role -> {
                throw new BadRequestException("Role already exists");
              });
    }
    if (!Objects.equals(roleName, newRoleName)) {
      roleRepository
          .findByName(newRoleName)
          .ifPresent(
              role -> {
                throw new BadRequestException("Role already exists");
              });
    }
  }
}
