package ecommerce_app.infrastructure.mapper;

import ecommerce_app.infrastructure.io.service.StaticResourceService;
import ecommerce_app.modules.order.model.dto.UserOrderResponse;
import ecommerce_app.modules.user.model.entity.User;
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
