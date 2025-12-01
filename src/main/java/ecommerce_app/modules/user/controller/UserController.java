package ecommerce_app.modules.user.controller;

import ecommerce_app.constant.message.ResponseMessageConstant;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.user.model.dto.UpdateUserRequest;
import ecommerce_app.modules.user.model.dto.UserRequest;
import ecommerce_app.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

  /**
   * Creates a new user based on the provided user data.
   *
   * @param userRequest The request object containing user information including optional multipart
   *     file.
   * @return A {@link ResponseEntity} containing a success message and created user data.
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseBodyResponse> createUser(@ModelAttribute UserRequest userRequest) {
    return BaseBodyResponse.success(
        this.userService.create(userRequest), ResponseMessageConstant.CREATE_SUCCESSFULLY);
  }

  /**
   * Updates the active status of an existing user.
   *
   * @param userId The ID of the user whose status needs to be updated.
   * @param status The new status to be set for the user (true for active, false for inactive).
   * @return A {@link ResponseEntity} indicating the operation was successful.
   */
  @PatchMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> updateStatus(
      @PathVariable(value = "id") Long userId,
      @RequestParam @Parameter(name = "status") Boolean status) {
    this.userService.updateStatus(userId, status);
    return BaseBodyResponse.success(null, ResponseMessageConstant.UPDATE_SUCCESSFULLY);
  }

  /**
   * Deletes a user by their ID.
   *
   * @param userId the ID of the user to delete
   * @return a success response if the user is deleted successfully
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> deleteUser(@PathVariable(value = "id") Long userId) {
    this.userService.deleteUser(userId);
    return BaseBodyResponse.success(null, ResponseMessageConstant.DELETE_SUCCESSFULLY);
  }

  /**
   * Retrieves a user by their ID.
   *
   * @param userId the ID of the user to retrieve
   * @return the user data in a success response
   */
  @GetMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> getById(@PathVariable(value = "id") Long userId) {
    return BaseBodyResponse.success(
        userService.findById(userId), ResponseMessageConstant.FIND_ONE_SUCCESSFULLY);
  }

  /**
   * Retrieves a user by their phone number.
   *
   * @param phone the phone number to search by
   * @return the user data in a success response
   */
  @GetMapping("/phone/{phone}")
  public ResponseEntity<BaseBodyResponse> getByPhone(@PathVariable(value = "phone") String phone) {
    return BaseBodyResponse.success(
        userService.findByPhone(phone), ResponseMessageConstant.FIND_ONE_SUCCESSFULLY);
  }

  /**
   * Retrieves a user by their email address.
   *
   * @param email the email address to search by
   * @return the user data in a success response
   */
  @GetMapping("/email/{email}")
  public ResponseEntity<BaseBodyResponse> getByEmail(@PathVariable(value = "email") String email) {
    return BaseBodyResponse.success(
        userService.findByEmail(email), ResponseMessageConstant.FIND_ONE_SUCCESSFULLY);
  }

  /**
   * Updates an existing user's information.
   *
   * @param updateUserRequest the request object containing updated user data
   * @param userId the ID of the user to update
   * @return the updated user data in a success response
   */
  @PutMapping("/{id}")
  public ResponseEntity<BaseBodyResponse> updateUser(
      @ModelAttribute UpdateUserRequest updateUserRequest,
      @PathVariable(value = "id") Long userId) {
    return BaseBodyResponse.success(
        userService.updateUser(updateUserRequest, userId),
        ResponseMessageConstant.UPDATE_SUCCESSFULLY);
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
  @GetMapping
  public ResponseEntity<BaseBodyResponse> filter(
      @RequestParam(value = "isPaged", defaultValue = "true") boolean isPaged,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
      @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
      @RequestParam(value = "sortDirection", defaultValue = "DESC") Sort.Direction sortDirection,
      @RequestParam(value = "filter", required = false) String filter) {
    return BaseBodyResponse.pageSuccess(
        userService.filter(isPaged, page, pageSize, sortBy, sortDirection, filter),
        ResponseMessageConstant.FIND_ALL_SUCCESSFULLY);
  }
}
