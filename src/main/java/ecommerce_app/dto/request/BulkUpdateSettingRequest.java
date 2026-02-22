package ecommerce_app.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class BulkUpdateSettingRequest {
  private List<BulkUpdateSettingItem> settings;
}
