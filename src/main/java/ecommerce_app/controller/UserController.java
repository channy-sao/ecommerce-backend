package ecommerce_app.controller;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.constant.message.ResponseMessageConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.request.AssignRoleToUserRequest;
import ecommerce_app.dto.request.CreateUserRequest;
import ecommerce_app.dto.request.UpdatePasswordRequest;
import ecommerce_app.dto.request.UpdateUserRequest;
import ecommerce_app.dto.response.UserResponse;
import ecommerce_app.service.UserService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for managing user-related operations for admin users. */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "For admin manage user")
public class UserController {
  private final UserService userService;
  private final MessageSourceService messageSourceService;

  // Prevent ModelMapper from being used in data binding
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.setDisallowedFields("roles", "addresses", "orders", "cart");
    binder.setValidator(null); // Disable any auto-validators
  }

  /**
   * Creates a new user based on the provided user data.
   *
   * @param createUserRequest The request object containing user information including optional
   *     multipart file.
   * @return A {@link ResponseEntity} containing a success message and created user data.
   */
  @PreAuthorize(
      "hasAuthority('USER_CREATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseBodyResponse<UserResponse>> createUser(
      @ModelAttribute CreateUserRequest createUserRequest) {
    return BaseBodyResponse.success(
        this.userService.create(createUserRequest),
        messageSourceService.getMessage(MessageKeyConstant.USER_MESSAGE_ADD_SUCCESS));
  }

  @Operation(summary = "Change user password")
  @PutMapping("change-password")
  public ResponseEntity<BaseBodyResponse<Void>> changePassword(
      @Valid @RequestBody UpdatePasswordRequest updateRequest) {
    this.userService.changePassword(updateRequest);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.AUTH_TITLE_CHANGE_PASSWORD));
  }

  /**
   * Updates the active status of an existing user.
   *
   * @param userId The ID of the user whose status needs to be updated.
   * @param status The new status to be set for the user (true for active, false for inactive).
   * @return A {@link ResponseEntity} indicating the operation was successful.
   */
  @PreAuthorize(
      "hasAuthority('USER_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
  @PatchMapping("/{id}/status")
  public ResponseEntity<BaseBodyResponse<Void>> updateStatus(
      @PathVariable(value = "id") Long userId,
      @RequestParam @Parameter(name = "status") Boolean status) {
    this.userService.updateStatus(userId, status);
    return BaseBodyResponse.success(ResponseMessageConstant.UPDATE_SUCCESSFULLY);
  }

  /**
   * Deletes a user by their ID.
   *
   * @param userId the ID of the user to delete
   * @return a success response if the user is deleted successfully
   */
  @PreAuthorize("hasAuthority('USER_DELETE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<Void>> deleteUser(
      @PathVariable(value = "id") Long userId) {
    this.userService.deleteUser(userId);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.USER_MESSAGE_DELETE_SUCCESS));
  }

  /**
   * Retrieves a user by their ID.
   *
   * @param userId the ID of the user to retrieve
   * @return the user data in a success response
   */
  @PreAuthorize(
      "hasAuthority('USER_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<UserResponse>> getById(
      @PathVariable(value = "id") Long userId) {
    return BaseBodyResponse.success(
        userService.findById(userId),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  /**
   * Retrieves a user by their phone number.
   *
   * @param phone the phone number to search by
   * @return the user data in a success response
   */
  @PreAuthorize(
      "hasAuthority('USER_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
  @GetMapping("/phone/{phone}")
  public ResponseEntity<BaseBodyResponse<UserResponse>> getByPhone(
      @PathVariable(value = "phone") String phone) {
    return BaseBodyResponse.success(
        userService.findByPhone(phone),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  /**
   * Retrieves a user by their email address.
   *
   * @param email the email address to search by
   * @return the user data in a success response
   */
  @PreAuthorize(
      "hasAuthority('USER_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
  @GetMapping("/email/{email}")
  public ResponseEntity<BaseBodyResponse<UserResponse>> getByEmail(
      @PathVariable(value = "email") String email) {
    return BaseBodyResponse.success(
        userService.findByEmail(email),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  /**
   * Updates an existing user's information.
   *
   * @param updateUserRequest the request object containing updated user data
   * @param userId the ID of the user to update
   * @return the updated user data in a success response
   */
  @PreAuthorize(
      "hasAuthority('USER_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
  @PutMapping("/{id}")
  public ResponseEntity<BaseBodyResponse<Void>> updateUser(
      @Parameter(description = "User update form data", required = true) @ModelAttribute @Valid
          UpdateUserRequest updateUserRequest,
      @Parameter(description = "User ID", required = true, example = "123")
          @PathVariable(value = "id")
          Long userId) {
    this.userService.updateUser(updateUserRequest, userId);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.USER_MESSAGE_UPDATE_SUCCESS));
  }

  /**
   * Filters users based on a search string applied to multiple fields (email, phone, first name,
   * last name), with support for pagination and sorting.
   *
   * @param isPaged flag indicating whether to apply pagination (default is true)
   * @param page the page number to retrieve (1-based index, default is 1)
   * @param pageSize the number of records per page (default is 10)
   * @param sortBy the field to sort by (e.g., "createdDate", default is "createdDate")
   * @param sortDirection the sort direction, either ASC or DESC (default is DESC)
   * @param filter the keyword to filter users by (optional)
   * @return a {@link ResponseEntity} containing a {@link BaseBodyResponse} with paginated user data
   */
  @PreAuthorize(
      "hasAuthority('USER_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<UserResponse>>> filter(
      @RequestParam(value = "isPaged", defaultValue = "true") boolean isPaged,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
      @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
      @RequestParam(value = "sortDirection", defaultValue = "DESC") Sort.Direction sortDirection,
      @RequestParam(value = "filter", required = false) String filter,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "roleId", required = false) Long roleId) {
    return BaseBodyResponse.pageSuccess(
        userService.filter(isPaged, page, pageSize, sortBy, sortDirection, filter, status, roleId),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PreAuthorize(
      "hasAuthority('USER_UPDATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
  @PutMapping("/{userId}/roles")
  public ResponseEntity<BaseBodyResponse<Void>> updateUserRoles(
      @PathVariable(value = "userId") Long userId, @RequestBody AssignRoleToUserRequest request) {

    userService.assignRoles(userId, request.getRoleIds());
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.USER_MESSAGE_UPDATE_SUCCESS));
  }
}
