package ecommerce_app.modules.setting.service;

import ecommerce_app.modules.setting.model.dto.SettingDto;

import java.math.BigDecimal;
import java.util.List;

public interface SettingService {

  String getString(String key);

  Integer getInt(String key);

  BigDecimal getDecimal(String key);

  SettingDto.Response update(String key, String value);

  List<SettingDto.Response> bulkUpdate(List<SettingDto.BulkUpdateItem> items);

  List<SettingDto.Response> getAll();
}
