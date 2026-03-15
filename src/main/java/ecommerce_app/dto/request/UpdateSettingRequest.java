package ecommerce_app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Schema(name = "UpdateSettingRequest", description = "Request object for updating a setting value")
public class UpdateSettingRequest {

  @NotBlank(message = "Setting value is required")
  @Size(max = 500, message = "Setting value must not exceed 500 characters")
  @Schema(description = "Value of the setting", example = "10")
  private String value;
}
