package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.PermissionEnum;
import ecommerce_app.entity.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Simple permission response")
public class SimplePermissionResponse {

  @Schema(description = "Permission ID", example = "1")
  private Long id;

  @Schema(description = "Permission name", example = "READ_USER")
  private PermissionEnum name;

  public static SimplePermissionResponse  toSimplePermissionResponse(final Permission permission) {
      return SimplePermissionResponse.builder()
          .id(permission.getId())
          .name(permission.getName())
          .build();
  }
}
