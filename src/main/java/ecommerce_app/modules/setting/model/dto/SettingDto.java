package ecommerce_app.modules.setting.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

public class SettingDto {

  @Data
  public static class UpdateRequest {
    @NotBlank private String value;
  }

  @Data
  public static class BulkUpdateRequest {
    private List<BulkUpdateItem> settings;
  }

  @Data
  public static class BulkUpdateItem {
    @NotBlank private String key;
    @NotBlank private String value;
  }

  @Data
  @Builder
  public static class Response {
    private String key;
    private String value;
    private String label;
    private LocalDateTime updatedAt;
  }
}
