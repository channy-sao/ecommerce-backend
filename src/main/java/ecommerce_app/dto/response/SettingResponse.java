package ecommerce_app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SettingResponse {
  private String key;
  private String value;
  private String label;
  private LocalDateTime updatedAt;
}
