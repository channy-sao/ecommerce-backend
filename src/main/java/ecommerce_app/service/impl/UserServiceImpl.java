package ecommerce_app.service.impl;

import ecommerce_app.config.DataInitializer;
import ecommerce_app.constant.enums.AuthProvider;
import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.core.io.service.FileManagerService;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.dto.request.CreateUserRequest;
import ecommerce_app.dto.request.UpdatePasswordRequest;
import ecommerce_app.dto.request.UpdateUserRequest;
import ecommerce_app.dto.response.UserResponse;
import ecommerce_app.entity.Role;
import ecommerce_app.entity.User;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ForbiddenException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.UserMapper;
import ecommerce_app.repository.RoleRepository;
import ecommerce_app.repository.UserRepository;
import ecommerce_app.service.UserService;
import ecommerce_app.specification.UserSpecification;
import ecommerce_app.util.MessageSourceService;
import jakarta.persistence.EntityManager;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;
  private final StorageConfig storageConfig;
  private final FileManagerService fileManagerService;
  private final UserMapper userMapper;
  private final MessageSourceService messageSourceService;

  @Transactional(readOnly = true)
  @Override
  public UserResponse findById(Long userId) {
    log.info("Find User by id: {}", userId);
    return getUserById(userId);
  }

  @Transactional(readOnly = true)
  @Override
  public UserResponse findByEmail(String email) {
    log.info("Find User by email: {}", email);
    final var user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        messageSourceService.getMessage(
                            MessageKeyConstant.USER_NOT_FOUND_EMAIL, email)));
    return userMapper.toUserResponse(user);
  }

  @Transactional(readOnly = true)
  @Override
  public UserResponse findByPhone(String phone) {
    log.info("Find User by phone: {}", phone);
    final var user =
        userRepository
            .findByPhone(phone)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        messageSourceService.getMessage(
                            MessageKeyConstant.USER_NOT_FOUND_PHONE, phone)));
    return userMapper.toUserResponse(user);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateUser(UpdateUserRequest request, Long userId) {

    try {
      final User user = findUserById(userId);
      if (userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId())) {
        log.error("User email is already in use");
        throw new BadRequestException(
            messageSourceService.getMessage(
                MessageKeyConstant.USER_EMAIL_ALREADY_USE, user.getEmail()));
      }

      validateUniqueFields(request, userId);

      // update ONLY simple fields
      user.setFirstName(request.getFirstName());
      user.setLastName(request.getLastName());

      if (request.getPhone() != null && !request.getPhone().isBlank()) {
        user.setPhone(request.getPhone());
      }

      if (request.getPassword() != null) {
        user.setPassword(passwordEncoder.encode(request.getPassword()));
      }

      // if include a new file in the request
      if (request.getProfile() != null && !request.getProfile().isEmpty()) {
        // remove old file if exist
        if (user.getAvatar() != null && !user.getAvatar().isBlank()) {
          deleteAvatarFile(user.getAvatar());
        }

        // save a new file
        String saveFile =
            fileManagerService.saveFile(request.getProfile(), storageConfig.getAvatarPath());
        user.setAvatar(saveFile);
      }
      userRepository.save(user);
      log.info("User updated: {}", user.getId());
    } catch (Exception ex) {
      log.error("FULL ERROR", ex);
      throw ex; // DO NOT WRAP IT
    }
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public UserResponse create(CreateUserRequest createUserRequest) {
    try {
      log.info("============ Creating user: {}", createUserRequest);
      if (userRepository.existsByEmail(createUserRequest.getEmail())) {
        log.error("User email {} is already in use", createUserRequest.getEmail());
        throw new BadRequestException(
            messageSourceService.getMessage(
                MessageKeyConstant.USER_EMAIL_ALREADY_USE, createUserRequest.getEmail()));
      }
      User user = modelMapper.map(createUserRequest, User.class);
      user.setAuthProvider(AuthProvider.LOCAL);
      if (createUserRequest.getPhone() != null && createUserRequest.getPhone().isBlank()) {
        user.setPhone(null);
      }
      user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
      User savedUser = userRepository.save(user);

      if (createUserRequest.getProfile() != null) {
        log.info("Uploading file: {}", createUserRequest.getProfile().getOriginalFilename());
        String avatarPath =
            fileManagerService.saveFile(
                createUserRequest.getProfile(), storageConfig.getAvatarPath());
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteUser(Long userId) {
    log.info("============= Deleting user: {}", userId);

    final User user = findUserById(userId);

    // prevent if the user is super admin
    if (user.getRoles().stream()
        .anyMatch(role -> role.getName().equals(DataInitializer.SUPER_ADMIN_ROLE))) {
      throw new ForbiddenException(
          messageSourceService.getMessage(
              MessageKeyConstant.USER_DELETE_SUPER_ADMIN, DataInitializer.SUPER_ADMIN_ROLE));
    }

    final String avatar = user.getAvatar();

    // Delete from database
    this.userRepository.delete(user);

    log.info("Deleted user with id {} from database", userId);

    // Delete it from storage (after transaction commits)
    if (avatar != null && !avatar.isBlank()) {
      deleteAvatarFile(avatar);
    }
  }

  public UserResponse getUserById(Long userId) {
    log.info("============= Get user by id: {}", userId);
    return userMapper.toUserResponse(findUserById(userId));
  }

  private User findUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", userId));
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateStatus(Long userId, Boolean status) {
    log.info("============= Updating status of user {}", userId);

    boolean isSuperAdminRole = userRepository.hasRole(userId, DataInitializer.SUPER_ADMIN_ROLE);

    if (Boolean.FALSE.equals(status) && isSuperAdminRole) {
      throw new BadRequestException(
          messageSourceService.getMessage(MessageKeyConstant.USER_DISABLE_SUPER_ADMIN));
    }

    // Use direct update query (avoids entity state issues)
    int updatedCount = userRepository.updateUserStatus(userId, status);

    if (updatedCount == 0) {
      throw new ResourceNotFoundException(
          messageSourceService.getMessage(MessageKeyConstant.USER_NOT_FOUND_ID, userId));
    }

    log.info("Updated status of user {} to {}", userId, status);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<UserResponse> filter(
      boolean isPage,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction direction,
      String filter,
      String status,
      Long roleId) {
    Specification<User> userSpecification =
        UserSpecification.byFilter(filter)
            .and(UserSpecification.byRoleId(roleId))
            .and(UserSpecification.byStatus(status));

    Sort sort = Sort.by(direction, sortBy);
    if (!isPage) {
      List<User> users = userRepository.findAll(userSpecification, sort);
      return new PageImpl<>(users.stream().map(userMapper::toUserResponse).toList());
    }

    PageRequest pageRequest = PageRequest.of(page - 1, pageSize, sort);
    return userRepository.findAll(userSpecification, pageRequest).map(userMapper::toUserResponse);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void changePassword(UpdatePasswordRequest changePasswordRequest) {
    log.info("============= Changing password for user: {}", changePasswordRequest.getEmail());
    final var user =
        userRepository
            .findByEmail(changePasswordRequest.getEmail())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        messageSourceService.getMessage(
                            MessageKeyConstant.USER_NOT_FOUND_EMAIL,
                            changePasswordRequest.getEmail())));

    if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
      log.error("Current password is not correct");
      throw new BadRequestException(
          messageSourceService.getMessage("error.user.password.current.incorrect"));
    }

    if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getOldPassword())) {
      log.error("New Password must be different from current password");
      throw new BadRequestException(
          messageSourceService.getMessage("error.user.password.same.as.current"));
    }

    if (!changePasswordRequest
        .getNewPassword()
        .equals(changePasswordRequest.getConfirmNewPassword())) {
      log.error("Confirm Password is not match");
      throw new BadRequestException(
          messageSourceService.getMessage("error.user.password.confirm.not.match"));
    }

    user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
    userRepository.save(user);
    log.info("Password changed successfully");
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void assignRoles(Long userId, Set<Long> roleIds) {
    try {
      log.info("============= START assignRoles for user: {}, roleIds: {}", userId, roleIds);

      // Fetch user with roles to avoid lazy loading
      User user =
          userRepository
              .findByIdWithRoles(userId) // Add this method
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          messageSourceService.getMessage(
                              MessageKeyConstant.USER_NOT_FOUND_ID, userId)));

      // Fetch new roles
      Set<Role> newRoles = new HashSet<>(roleRepository.findAllById(roleIds));

      if (newRoles.size() != roleIds.size()) {
        Set<Long> foundIds = newRoles.stream().map(Role::getId).collect(Collectors.toSet());
        Set<Long> missingIds =
            roleIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet());
        log.error("Roles not found: {}", missingIds);
        throw new ResourceNotFoundException(
            messageSourceService.getMessage("error.role.not.found.ids", missingIds));
      }

      // Check if the user is admin and trying to remove all roles
      boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));

      if (isAdmin && newRoles.isEmpty()) {
        throw new BadRequestException(
            messageSourceService.getMessage("error.role.admin.must.have.one"));
      }

      // Update roles
      user.getRoles().clear();
      user.getRoles().addAll(newRoles);
      userRepository.save(user);

      log.info("=== END assignRoles - SUCCESS for user: {}", userId);

    } catch (DataIntegrityViolationException e) {
      log.error("=== Database constraint violation", e);
      throw new BadRequestException(
          messageSourceService.getMessage("error.role.assignment.duplicate"));
    } catch (BadRequestException | ResourceNotFoundException e) {
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during assignRoles for user: {}", userId, e);
      throw new BadRequestException(
          messageSourceService.getMessage("error.role.assignment.failed", userId));
    }
  }

  private void validateUniqueFields(UpdateUserRequest request, Long userId) {

    if (request.getEmail() != null
        && userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
      throw new BadRequestException(
          messageSourceService.getMessage(
              MessageKeyConstant.USER_EMAIL_ALREADY_USE, request.getEmail()));
    }

    if (request.getPhone() != null
        && userRepository.existsByPhoneAndIdNot(request.getPhone(), userId)) {
      throw new BadRequestException(
          messageSourceService.getMessage("error.user.phone.already.in.use", request.getPhone()));
    }
  }

  /** Delete an avatar file outside of transaction to avoid rollback */
  @Async
  public void deleteAvatarFile(String avatarPath) {
    try {
      fileManagerService.deleteFile(storageConfig.getAvatarPath(), avatarPath);
      log.info("Deleted avatar file: {}", avatarPath);
    } catch (Exception e) {
      log.error("Failed to delete avatar file {}: {}", avatarPath, e.getMessage());
    }
  }

  /** Clean up failed upload */
  private void cleanupFailedUpload(String avatarPath) {
    try {
      fileManagerService.deleteFile(storageConfig.getAvatarPath(), avatarPath);
      log.warn("Cleaned up failed upload: {}", avatarPath);
    } catch (Exception e) {
      log.error("Failed to cleanup failed upload {}: {}", avatarPath, e.getMessage());
    }
  }
}
