package ecommerce_app.modules.user.service;

import ecommerce_app.modules.user.model.dto.CreateUserRequest;
import ecommerce_app.modules.user.model.dto.UpdatePasswordRequest;
import ecommerce_app.modules.user.model.dto.UpdateUserRequest;
import ecommerce_app.modules.user.model.dto.UserResponse;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface UserService {
  UserResponse findById(Long userId);

  UserResponse findByEmail(String email);

  UserResponse findByPhone(String phone);

  void updateUser(UpdateUserRequest updateRequest, Long userId);

  UserResponse create(CreateUserRequest createUserRequest);

  void deleteUser(Long userId);

  void updateStatus(Long userId, Boolean status);

  Page<UserResponse> filter(
      boolean isPage,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction direction,
      String filter);

  void changePassword(UpdatePasswordRequest changePasswordRequest);

  void assignRoles(Long userId, Set<Long> roleIds);
}
