package ecommerce_app.modules.user.service;

import ecommerce_app.modules.user.model.dto.CreateRoleRequest;
import ecommerce_app.modules.user.model.dto.RoleResponse;
import ecommerce_app.modules.user.model.dto.UpdateRoleRequest;

import java.util.Set;

public interface RoleService {
  void createRole(CreateRoleRequest createRoleRequest);

  void updateRole(UpdateRoleRequest updateRoleRequest, long roleId);

  void deleteRole(long roleId);

  RoleResponse getRole(long roleId);

  Set<RoleResponse> getRoles();

  Set<RoleResponse> searchRole(String roleName);
}
