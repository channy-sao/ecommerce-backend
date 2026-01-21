package ecommerce_app.modules.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "UserOrderResponse", description = "User order representation contras from User entity")
public class UserOrderResponse {

  @Schema(description = "User ID", example = "1")
  private Long id;

  @Schema(description = "User email address", example = "user@example.com")
  private String email;

  @Schema(description = "Full Name of the user", example = "John Doe")
  private String fullName;

  @Schema(description = "User phone number", example = "+85512345678")
  private String phone;

  @Schema(
      description = "URL to the user's avatar image",
      example = "https://example.com/images/avatar.png")
  private String avatar;
}
