package ecommerce_app.modules.user.service.impl;

import com.google.firebase.database.DatabaseException;
import ecommerce_app.config.DataInitializer;
import ecommerce_app.infrastructure.exception.BadRequestException;
import ecommerce_app.infrastructure.exception.ForbiddenException;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.mapper.UserMapper;
import ecommerce_app.infrastructure.property.StorageConfigProperty;
import ecommerce_app.modules.user.model.dto.CreateUserRequest;
import ecommerce_app.modules.user.model.dto.UpdatePasswordRequest;
import ecommerce_app.modules.user.model.dto.UpdateUserRequest;
import ecommerce_app.modules.user.model.dto.UserResponse;
import ecommerce_app.modules.user.model.entity.Role;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.RoleRepository;
import ecommerce_app.modules.user.repository.UserRepository;
import ecommerce_app.modules.user.service.UserService;
import ecommerce_app.modules.user.specification.UserSpecification;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Implementation of {@link UserService} that handles user-related business logic, including
 * creation, update, deletion, and status management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;
  private final StorageConfigProperty storageConfigProperty;
  private final FileManagerService fileManagerService;
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  @Override
  public UserResponse findById(Long userId) {
    log.info("Find User by id: {}", userId);
    return getUserById(userId);
  }

  /**
   * Finds a user by email.
   *
   * @param email the email to search for
   * @return the {@link User} if found, otherwise null
   */
  @Override
  public UserResponse findByEmail(String email) {
    log.info("Find User by email: {}", email);
    final var user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with email " + email + " not found"));
    return userMapper.toUserResponse(user);
  }

  /**
   * Finds a user by phone number.
   *
   * @param phone the phone number to search for
   * @return the {@link User} if found, otherwise null
   */
  @Override
  public UserResponse findByPhone(String phone) {
    log.info("Find User by phone: {}", phone);
    final var user =
        userRepository
            .findByPhone(phone)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with phone " + phone + " not found"));
    return userMapper.toUserResponse(user);
  }

  /**
   * Updates the user information and optionally updates the avatar image.
   *
   * @param updateUserRequest the user request data
   * @param userId the ID of the user to update
   * @return the updated {@link User} entity
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public UserResponse updateUser(UpdateUserRequest updateUserRequest, Long userId) {
    log.info("Update User by id: {}", userId);
    try {
      final User existing = findUserById(userId);
      final String existingAvatar = existing.getAvatar();
      // update user in DB
      modelMapper.map(updateUserRequest, existing);
      if (updateUserRequest.getPassword() != null) {
        existing.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
      }
      User saved = userRepository.save(existing);

      // update avatar file
      if (updateUserRequest.getProfile() != null && !updateUserRequest.getProfile().isEmpty()) {
        // remove an exiting file and upload new
        this.fileManagerService.deleteFile(storageConfigProperty.getAvatar(), existingAvatar);
        String savedAvatar =
            fileManagerService.saveFile(
                updateUserRequest.getProfile(), storageConfigProperty.getAvatar());
        saved.setAvatar(savedAvatar);
      }

      return userMapper.toUserResponse(userRepository.save(saved));
    } catch (DataIntegrityViolationException e) {
      log.error(e.getMessage(), e);
      throw new BadRequestException(e.getMessage());
    }
  }

  /**
   * Creates a new user with encrypted password and optional avatar image upload.
   *
   * @param createUserRequest the user request data
   * @return the created {@link User} entity
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public UserResponse create(CreateUserRequest createUserRequest) {
    try {
      log.info("Creating user: {}", createUserRequest);
      // save user to a database
      User user = modelMapper.map(createUserRequest, User.class);
      user.setProvider("LOCAL");
      // encrypt password
      user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
      User savedUser = userRepository.save(user);

      // save image avatar of user to storage
      if (createUserRequest.getProfile() != null) {
        log.info("Uploading file: {}", createUserRequest.getProfile().getOriginalFilename());
        String avatarPath =
            fileManagerService.saveFile(
                createUserRequest.getProfile(), storageConfigProperty.getAvatar());
        if (avatarPath != null) {
          log.info("Uploading avatar: {}", avatarPath);
          savedUser.setAvatar(avatarPath);
          return userMapper.toUserResponse(userRepository.save(savedUser));
        }
      }
      return userMapper.toUserResponse(savedUser);
    } catch (DataIntegrityViolationException exception) {
      log.error(exception.getMessage(), exception);
      throw new BadRequestException(exception.getMessage());
    }
  }

  /**
   * Deletes a user by ID from the database. Storage cleanup can be implemented as needed.
   *
   * @param userId the ID of the user to delete
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteUser(Long userId) {
    log.info("Deleting user: {}", userId);
    final User user = findUserById(userId);

    // prevent if user is super admin
    if (user.getRoles().stream()
        .anyMatch(role -> role.getName().equals(DataInitializer.SUPER_ADMIN_ROLE))) {
      throw new ForbiddenException(
          String.format("Could not delete user %s", DataInitializer.SUPER_ADMIN_ROLE));
    }

    // delete from database
    this.userRepository.deleteById(userId);

    // delete from storage
    final String avatar = user.getAvatar();
    this.fileManagerService.deleteFile(storageConfigProperty.getAvatar(), avatar);
    log.info("Deleted user with id {}", userId);
  }

  /**
   * Retrieves a user by their ID.
   *
   * @param userId the ID of the user to retrieve
   * @return the {@link User} entity if found
   * @throws ResourceNotFoundException if no user with the specified ID exists
   */
  public UserResponse getUserById(Long userId) {
    log.info("Get user by id: {}", userId);
    return userMapper.toUserResponse(findUserById(userId));
  }

  private User findUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", userId));
  }

  /**
   * Updates the active status of a user.
   *
   * @param userId the ID of the user to update
   * @param status the new active status (true = active, false = inactive)
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateStatus(Long userId, Boolean status) {
    log.info("Updating status of user {}", userId);
    var user = findUserById(userId);

    // prevent disable user super admin
    if (Boolean.TRUE.equals(status)
        || user.getRoles().stream()
            .noneMatch(role -> role.getName().equals(DataInitializer.SUPER_ADMIN_ROLE))) {
      user.setIsActive(status);
      userRepository.save(user);

      log.info("Updated status of user {}", userId);
    } else {
      throw new BadRequestException("Could not disable super administrator");
    }
  }

  /**
   * Filters users based on a search string applied to multiple fields (email, phone, first name,
   * last name).
   *
   * @param isPage whether to apply pagination (currently not used in this implementation but can be
   *     extended)
   * @param page the page number to retrieve (zero-based index)
   * @param pageSize the number of records per page
   * @param sortBy the field to sort by (e.g., "firstName", "email")
   * @param direction the direction of the sort (ASC or DESC)
   * @param filter the search keyword to filter user fields (case-insensitive and partial match)
   * @return a {@link Page} of {@link User} entities matching the filter and pagination/sorting
   *     criteria
   */
  @Transactional(readOnly = true)
  @Override
  public Page<UserResponse> filter(
      boolean isPage,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction direction,
      String filter) {
    Specification<User> userSpecification = UserSpecification.byFilter(filter);
    Sort sort = Sort.by(direction, sortBy);
    if (!isPage) {
      List<User> users = userRepository.findAll(userSpecification, sort);
      return new PageImpl<>(users.stream().map(userMapper::toUserResponse).toList());
    }
    // default it starts from zero
    PageRequest pageRequest = PageRequest.of(page - 1, pageSize, sort);

    return userRepository.findAll(userSpecification, pageRequest).map(userMapper::toUserResponse);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void changePassword(UpdatePasswordRequest changePasswordRequest) {
    log.info("Changing password for user: {}", changePasswordRequest.getEmail());
    final var user =
        userRepository
            .findByEmail(changePasswordRequest.getEmail())
            .orElseThrow(
                () -> new ResourceNotFoundException("User", changePasswordRequest.getEmail()));

    // verify user password
    if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
      log.error("Current password is not correct");
      throw new BadRequestException("Current password is not correct");
    }

    // verify password is difference
    if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getOldPassword())) {
      log.error("New Password must be different from current password");
      throw new BadRequestException("New Password must be different from current password");
    }

    // 4. Validate password confirmation
    if (!changePasswordRequest
        .getNewPassword()
        .equals(changePasswordRequest.getConfirmNewPassword())) {
      log.error("Confirm Password is not match");
      throw new BadRequestException("Confirm Password is not match");
    }

    user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
    userRepository.save(user);
    log.info("Password changed successfully");
  }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void assignRoles(Long userId, Set<Long> roleIds) {
        try {
            log.info("=== START assignRoles for user: {}, roleIds: {}", userId, roleIds);

            // 1. Fetch user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            log.debug("Found user: {}", user.getEmail());

            // 2. Check current roles
            Set<String> currentRoleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            log.debug("Current roles: {}", currentRoleNames);

            boolean isAdmin = currentRoleNames.contains("ADMIN");
            log.debug("Is admin: {}", isAdmin);

            // 3. Fetch new roles
            Set<Role> newRoles = new HashSet<>(roleRepository.findAllById(roleIds));
            Set<String> newRoleNames = newRoles.stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            log.debug("New roles to assign: {}", newRoleNames);

            // 4. Validation
            if (isAdmin && newRoles.isEmpty()) {
                log.error("Admin user cannot have empty roles");
                throw new BadRequestException("Admin user must have at least one role");
            }

            if (newRoles.size() != roleIds.size()) {
                Set<Long> foundIds = newRoles.stream().map(Role::getId).collect(Collectors.toSet());
                Set<Long> missingIds = roleIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toSet());
                log.error("Roles not found: {}", missingIds);
                throw new ResourceNotFoundException("Roles not found: " + missingIds);
            }

            // 5. Clear existing (to avoid duplicate key violations)
            log.debug("Clearing existing roles...");
            user.getRoles().clear();
            userRepository.saveAndFlush(user); // Force flush to clear

            // 6. Assign new roles
            log.debug("Assigning new roles...");
            user.setRoles(newRoles);
            User savedUser = userRepository.save(user);

            log.info("=== END assignRoles - SUCCESS for user: {}", userId);

        } catch (DataIntegrityViolationException e) {
            log.error("=== Database constraint violation", e);
            throw new DatabaseException("Role assignment failed: " + e.getMostSpecificCause().getMessage(), e);
        } catch (BadRequestException | ResourceNotFoundException e) {
            log.error("=== Business rule violation", e);
            throw e; // Re-throw business exceptions
        } catch (Exception e) {
            log.error("=== Unexpected error", e);
            throw new DatabaseException("Could not assign roles for user " + userId, e);
        }
    }
}
