package ecommerce_app.controller.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.request.CreateRoleRequest;
import ecommerce_app.dto.response.RoleResponse;
import ecommerce_app.dto.request.UpdateRoleRequest;
import ecommerce_app.service.RoleService;
import ecommerce_app.util.MessageSourceService;
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

import java.util.Set;

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
  private final MessageSourceService messageSourceService;

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
  public ResponseEntity<BaseBodyResponse<Void>> createRole(
      @Valid @RequestBody CreateRoleRequest createRoleRequest) {

    roleService.createRole(createRoleRequest);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SAVE_SUCCESS));
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
  public ResponseEntity<BaseBodyResponse<Void>> updateRole(
      @Valid @RequestBody UpdateRoleRequest updateRoleRequest, @PathVariable("id") Long roleId) {

    roleService.updateRole(updateRoleRequest, roleId);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_UPDATE_SUCCESS));
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
  public ResponseEntity<BaseBodyResponse<Void>> toggleStatus(@PathVariable("id") Long roleId) {

    roleService.toggleStatus(roleId);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_UPDATE_SUCCESS));
  }

  @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN') or hasAuthority('ROLE_READ')")
  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<RoleResponse>> getRoleById(
      @PathVariable(value = "id") long roleId) {
    return BaseBodyResponse.success(
        roleService.getRole(roleId),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PreAuthorize("hasAuthority('ROLE_READ') or hasAnyRole('ADMIN','SUPER_ADMIN')")
  @GetMapping
  public ResponseEntity<BaseBodyResponse<Set<RoleResponse>>> getRoles() {
    return BaseBodyResponse.success(
        roleService.getRoles(),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PreAuthorize("hasAuthority('ROLE_READ') or hasAnyRole('ADMIN','SUPER_ADMIN')")
  @GetMapping("/search")
  public ResponseEntity<BaseBodyResponse<Set<RoleResponse>>> searchRole(
      @Parameter(name = "role") String roleName) {
    return BaseBodyResponse.success(
        roleService.searchRole(roleName),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
