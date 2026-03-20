package ecommerce_app.service;

import ecommerce_app.dto.request.CreateRoleRequest;
import ecommerce_app.dto.response.RoleResponse;
import ecommerce_app.dto.request.UpdateRoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.Set;

public interface RoleService {
  void createRole(CreateRoleRequest createRoleRequest);

  void updateRole(UpdateRoleRequest updateRoleRequest, long roleId);

  void toggleStatus(long roleId);

  RoleResponse getRole(long roleId);

  Set<RoleResponse> getRoles();

  Page<RoleResponse> searchRole(String roleName, int page, int pageSize, String sortBy, Sort.Direction sortDirection);
}
