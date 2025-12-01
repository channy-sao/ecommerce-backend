package ecommerce_app.modules.user.service;

import ecommerce_app.modules.user.model.dto.UpdateUserRequest;
import ecommerce_app.modules.user.model.dto.UserRequest;
import ecommerce_app.modules.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface UserService {
  User findById(Long userId);

  User findByEmail(String email);

  User findByPhone(String phone);

  User updateUser(UpdateUserRequest updateRequest, Long userId);

  User create(UserRequest userRequest);

  void deleteUser(Long userId);

  void updateStatus(Long userId, Boolean status);

  Page<User> filter(
      boolean isPage,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction direction,
      String filter);
}
