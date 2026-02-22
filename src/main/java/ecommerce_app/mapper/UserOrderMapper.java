package ecommerce_app.mapper;

import ecommerce_app.core.io.service.StaticResourceService;
import ecommerce_app.dto.response.UserOrderResponse;
import ecommerce_app.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserOrderMapper {
  private final StaticResourceService staticResourceService;

  public UserOrderResponse toUserOrder(User user) {
    return UserOrderResponse.builder()
        .id(user.getId())
        .phone(user.getPhone())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .avatar(staticResourceService.getUserAvatarUrl(user.getAvatar()))
        .build();
  }
}
