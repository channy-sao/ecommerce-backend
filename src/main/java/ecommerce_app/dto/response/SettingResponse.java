package ecommerce_app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Application setting response")
public class SettingResponse {

  @Schema(description = "Unique setting key", example = "site.maintenance_mode")
  private String key;

  @Schema(description = "Value of the setting", example = "false")
  private String value;

  @Schema(description = "Human-readable label for the setting", example = "Maintenance Mode")
  private String label;

  @Schema(
      description = "Date and time the setting was last updated",
      example = "2024-01-01T10:00:00")
  private LocalDateTime updatedAt;
}
