package ecommerce_app.mapper;

import ecommerce_app.core.io.service.FileManagerService;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.dto.response.RoleResponse;
import ecommerce_app.dto.response.SimplePermissionResponse;
import ecommerce_app.dto.response.SimpleRoleResponse;
import ecommerce_app.dto.response.UserResponse;
import ecommerce_app.entity.Permission;
import ecommerce_app.entity.Role;
import ecommerce_app.entity.User;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Utility class responsible for mapping {@link User} entities to DTO responses.
 *
 * <p>This mapper converts domain entities into API-friendly response objects, including roles and
 * aggregated permissions derived from assigned roles.
 *
 * <p>This class is not intended to be instantiated.
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

  private final StorageConfig storageConfig;
  private final FileManagerService fileManagerService;

  /**
   * Maps a {@link User} entity to a {@link UserResponse}.
   *
   * <p>The resulting response includes:
   *
   * <ul>
   *   <li>User ID
   *   <li>Assigned roles
   *   <li>Aggregated permissions from all roles
   * </ul>
   *
   * @param user the user entity to map
   * @return a populated {@link UserResponse}
   */
  public UserResponse toUserResponse(User user) {
    UserResponse userResponse = new UserResponse();
    userResponse.setId(user.getId());
    userResponse.setEmail(user.getEmail());
    userResponse.setFirstName(user.getFirstName());
    userResponse.setLastName(user.getLastName());
    userResponse.setPhone(user.getPhone());
    userResponse.setIsActive(user.getIsActive());
    userResponse.setFullName(getFullName(user));
    userResponse.setAvatar(getAvatarUrl(user.getAvatar()));
    userResponse.setRoles(getRoleResponses(user.getRoles()));
    userResponse.setPermissions(getPermissions(user.getRoles()));
    return userResponse;
  }

  private String getFullName(final User user) {
    return user.getFirstName() + " " + user.getLastName();
  }

  /**
   * Converts a set of {@link Role} entities to a set of {@link SimpleRoleResponse} DTOs.
   *
   * @param roles the roles assigned to a user
   * @return a set of role responses, or an empty set if roles are null or empty
   */
  public static Set<SimpleRoleResponse> getRoleResponses(Set<Role> roles) {
    if (CollectionUtils.isEmpty(roles)) {
      return Collections.emptySet();
    }
    return roles.stream().map(SimpleRoleResponse::roleResponse).collect(Collectors.toSet());
  }

  /**
   * Extracts and aggregates permissions from a set of {@link Role} entities.
   *
   * <p>Permissions are flattened across all roles and duplicates are removed.
   *
   * @param roles the roles assigned to a user
   * @return a unique set of permissions, or an empty set if roles are null or empty
   */
  public static Set<SimplePermissionResponse> getPermissions(Set<Role> roles) {
    if (CollectionUtils.isEmpty(roles)) {
      return Collections.emptySet();
    }
    return roles.stream()
        .flatMap(role -> role.getPermissions().stream())
        .map(SimplePermissionResponse::toSimplePermissionResponse)
        .collect(Collectors.toSet());
  }

  private String getAvatarUrl(String avatar) {

    if (avatar == null) {
      return null;
    }
    // if avatar from auth provider (e.g., Google Firebase)
    if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
      return avatar;
    }

    // else get from our storage with concatenated url
    return fileManagerService.getResourceUrl(storageConfig.getAvatarPath(), avatar);
  }
}
