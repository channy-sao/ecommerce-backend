package ecommerce_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BulkUpdateSettingItem {
  @NotBlank private String key;
  @NotBlank private String value;
}
