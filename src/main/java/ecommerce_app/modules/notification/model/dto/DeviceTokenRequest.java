package ecommerce_app.modules.notification.model.dto;

import ecommerce_app.constant.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class DeviceTokenRequest {

  @NotBlank(message = "Token is required")
  private String token;

  @NotNull(message = "Device type is required")
  private DeviceType deviceType;

  private String deviceName;
  private String deviceModel;
  private String osVersion;
  private String appVersion;
}
