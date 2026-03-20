package ecommerce_app.service.impl;

import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.dto.request.CreateRoleRequest;
import ecommerce_app.dto.response.RoleResponse;
import ecommerce_app.dto.request.UpdateRoleRequest;
import ecommerce_app.entity.Role;
import ecommerce_app.repository.PermissionRepository;
import ecommerce_app.repository.RoleRepository;
import ecommerce_app.service.RoleService;
import ecommerce_app.service.ValidatePermission;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    role.setName(createRoleRequest.getRoleName().trim());
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
              role.setName(updateRoleRequest.getRoleName().trim());
              role.setPermissions(new HashSet<>(permissions));
              role.setDescription(updateRoleRequest.getDescription());
              roleRepository.save(role);
            },
            () -> {
              throw new ResourceNotFoundException("Role", roleId);
            });
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void toggleStatus(long roleId) {
    log.info("Start soft delete role id={}", roleId);

    final Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
    role.setActive(!role.isActive());
    roleRepository.save(role);

    log.info("Update status role id={}", roleId);
  }

  @Transactional(readOnly = true)
  @Override
  public RoleResponse getRole(long roleId) {
    log.info("Start get role id={}", roleId);
    final var role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
    return modelMapper.map(role, RoleResponse.class);
  }

  @Transactional(readOnly = true)
  @Override
  public Set<RoleResponse> getRoles() {
    log.info("Start get roles.");
    Sort sort = Sort.by(Sort.Direction.ASC, "name");
    return roleRepository.findAll(sort).stream()
        .map(role -> modelMapper.map(role, RoleResponse.class))
        .collect(Collectors.toSet());
  }

  @Transactional(readOnly = true)
  @Override
  public Page<RoleResponse> searchRole(
      String roleName, int page, int pageSize, String sortBy, Sort.Direction sortDirection) {
    Sort sort = Sort.by(sortDirection, sortBy);
    return roleRepository
        .findByNameContainingIgnoreCase(roleName, PageRequest.of(page - 1, pageSize, sort))
        .map(role -> modelMapper.map(role, RoleResponse.class));
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
