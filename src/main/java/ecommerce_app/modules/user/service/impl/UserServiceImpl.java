package ecommerce_app.modules.user.service.impl;

import ecommerce_app.infrastructure.exception.BadRequestException;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.io.service.FileManagerService;
import ecommerce_app.infrastructure.property.StorageConfigProperty;
import ecommerce_app.modules.user.model.dto.UpdateUserRequest;
import ecommerce_app.modules.user.model.dto.UserRequest;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import ecommerce_app.modules.user.service.UserService;
import ecommerce_app.modules.user.specification.UserSpecification;
import java.util.List;
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

/**
 * Implementation of {@link UserService} that handles user-related business logic, including
 * creation, update, deletion, and status management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;
  private final StorageConfigProperty storageConfigProperty;
  private final FileManagerService fileManagerService;

  @Transactional(readOnly = true)
  @Override
  public User findById(Long userId) {
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
  public User findByEmail(String email) {
    log.info("Find User by email: {}", email);
    return userRepository
        .findByEmail(email)
        .orElseThrow(
            () -> new ResourceNotFoundException("User with email " + email + " not found"));
  }

  /**
   * Finds a user by phone number.
   *
   * @param phone the phone number to search for
   * @return the {@link User} if found, otherwise null
   */
  @Override
  public User findByPhone(String phone) {
    log.info("Find User by phone: {}", phone);
    return userRepository.findByPhone(phone);
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
  public User updateUser(UpdateUserRequest updateUserRequest, Long userId) {
    log.info("Update User by id: {}", userId);
    try {
      final User existing = getUserById(userId);
      final String existingAvatar = existing.getAvatar();
      // update user in DB
      modelMapper.map(updateUserRequest, existing);
      User saved = userRepository.save(existing);

      // update avatar file
      if (updateUserRequest.getMultipartFile() != null) {
        // remove an exiting file and upload new
        this.fileManagerService.deleteFile(storageConfigProperty.getAvatar(), existingAvatar);
        String savedAvatar =
            fileManagerService.saveFile(
                updateUserRequest.getMultipartFile(), storageConfigProperty.getAvatar());
        saved.setAvatar(savedAvatar);

      } else {
        // remove avatar file
        this.fileManagerService.deleteFile(storageConfigProperty.getAvatar(), existingAvatar);
        // remove from db
        saved.setAvatar(null);
      }

      return userRepository.save(saved);
    } catch (DataIntegrityViolationException e) {
      log.error(e.getMessage(), e);
      throw new BadRequestException(e.getMessage());
    }
  }

  /**
   * Creates a new user with encrypted password and optional avatar image upload.
   *
   * @param userRequest the user request data
   * @return the created {@link User} entity
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public User create(UserRequest userRequest) {
    try {
      log.info("Creating user: {}", userRequest);
      // save user to a database
      User user = modelMapper.map(userRequest, User.class);
      user.setProvider("LOCAL");
      // encrypt password
      user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
      User savedUser = userRepository.save(user);

      // save image avatar of user to storage
      if (userRequest.getMultipartFile() != null) {
        log.info("Uploading file: {}", userRequest.getMultipartFile().getOriginalFilename());
        String avatarPath =
            fileManagerService.saveFile(
                userRequest.getMultipartFile(), storageConfigProperty.getAvatar());
        if (avatarPath != null) {
          log.info("Uploading avatar: {}", avatarPath);
          savedUser.setAvatar(avatarPath);
          return userRepository.save(savedUser);
        }
      }
      return savedUser;
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
    final User user = getUserById(userId);
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
  public User getUserById(Long userId) {
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
    this.userRepository
        .findById(userId)
        .ifPresent(
            user -> {
              user.setIsActive(status);
              userRepository.save(user);
            });
    log.info("Updated status of user {}", userId);
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
  public Page<User> filter(
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
      return new PageImpl<>(users);
    }
    // default it starts from zero
    PageRequest pageRequest = PageRequest.of(page - 1, pageSize, sort);

    return userRepository.findAll(userSpecification, pageRequest);
  }
}
