package ecommerce_app.service;

import ecommerce_app.dto.request.BulkUpdateSettingItem;
import ecommerce_app.dto.response.SettingResponse;

import java.math.BigDecimal;
import java.util.List;

public interface SettingService {

  String getString(String key);

  Integer getInt(String key);

  BigDecimal getDecimal(String key);

  SettingResponse update(String key, String value);

  List<SettingResponse> bulkUpdate(List<BulkUpdateSettingItem> items);

  List<SettingResponse> getAll();
}
