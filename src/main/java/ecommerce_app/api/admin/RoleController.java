package ecommerce_app.api.admin;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.user.model.dto.CreateRoleRequest;
import ecommerce_app.modules.user.model.dto.UpdateRoleRequest;
import ecommerce_app.modules.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Role Management API.
 *
 * <p>Provides endpoints for managing system roles:
 *
 * <ul>
 *   <li>Create role
 *   <li>Update role
 *   <li>Soft delete role
 * </ul>
 *
 * <p>This controller is restricted to admin users.
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/admin/v1/roles")
@Tag(name = "Role Management", description = "For admin manage roles")
public class RoleController {

  private final RoleService roleService;

  /**
   * Create a new role.
   *
   * @param createRoleRequest role creation payload
   * @return success response
   */
  @Operation(
      summary = "Create new role",
      description = "Create a new role and assign permissions to it")
  @ApiResponse(
      responseCode = "200",
      description = "Role created successfully",
      content = @Content(schema = @Schema(implementation = BaseBodyResponse.class)))
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @PostMapping
  public ResponseEntity<BaseBodyResponse> createRole(
      @Valid @RequestBody CreateRoleRequest createRoleRequest) {

    roleService.createRole(createRoleRequest);
    return BaseBodyResponse.success(null, "Create Role Successfully");
  }

  /**
   * Update an existing role.
   *
   * @param updateRoleRequest role update payload
   * @param roleId role identifier
   * @return success response
   */
  @Operation(
      summary = "Update role",
      description = "Update role name, description, and permissions")
  @ApiResponse(
      responseCode = "200",
      description = "Role updated successfully",
      content = @Content(schema = @Schema(implementation = BaseBodyResponse.class)))
  @PreAuthorize("hasAuthority('ROLE_UPDATE')")
  @PutMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> updateRole(
      @Valid @RequestBody UpdateRoleRequest updateRoleRequest, @PathVariable("id") Long roleId) {

    roleService.updateRole(updateRoleRequest, roleId);
    return BaseBodyResponse.success(null, "Update Role Successfully");
  }

  /**
   * Toggle status of role.
   *
   * <p>The role will not be physically removed from the database. Instead, it will be marked as
   * deleted.
   *
   * @param roleId role identifier
   * @return success response
   */
  @Operation(summary = "Toggle status role", description = "Toggle status role by id")
  @ApiResponse(
      responseCode = "200",
      description = "Toggle Role status successfully",
      content = @Content(schema = @Schema(implementation = BaseBodyResponse.class)))
  @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('ROLE_UPDATE')")
  @PatchMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> toggleStatus(@PathVariable("id") Long roleId) {

    roleService.toggleStatus(roleId);
    return BaseBodyResponse.success(null, "Toggle Status Role Successfully");
  }

  @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_CREATE')")
  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> getRoleById(@PathVariable(value = "id") long roleId) {
    return BaseBodyResponse.success(roleService.getRole(roleId), "Get Role Successfully");
  }

  @PreAuthorize("hasAuthority('ROLE_READ')")
  @GetMapping
  public ResponseEntity<BaseBodyResponse> getRoles() {
    return BaseBodyResponse.success(roleService.getRoles(), "Get Roles Successfully");
  }

  @PreAuthorize("hasAuthority('ROLE_READ')")
  @GetMapping("/search")
  public ResponseEntity<BaseBodyResponse> searchRole(@Parameter(name = "role") String roleName) {
    return BaseBodyResponse.success(roleService.searchRole(roleName), "Get Roles Successfully");
  }
}
