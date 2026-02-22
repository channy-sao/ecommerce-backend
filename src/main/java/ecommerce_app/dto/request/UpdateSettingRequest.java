package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSettingRequest {
  @NotBlank private String value;
}
