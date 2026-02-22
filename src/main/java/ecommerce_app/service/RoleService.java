package ecommerce_app.service;

import ecommerce_app.dto.request.CreateRoleRequest;
import ecommerce_app.dto.response.RoleResponse;
import ecommerce_app.dto.request.UpdateRoleRequest;

import java.util.Set;

public interface RoleService {
  void createRole(CreateRoleRequest createRoleRequest);

  void updateRole(UpdateRoleRequest updateRoleRequest, long roleId);

  void toggleStatus(long roleId);

  RoleResponse getRole(long roleId);

  Set<RoleResponse> getRoles();

  Set<RoleResponse> searchRole(String roleName);
}
