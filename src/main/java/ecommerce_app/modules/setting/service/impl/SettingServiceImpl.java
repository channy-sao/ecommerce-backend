package ecommerce_app.modules.setting.service.impl;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.setting.model.dto.SettingDto;
import ecommerce_app.modules.setting.model.entity.Setting;
import ecommerce_app.modules.setting.repository.SettingRepository;
import ecommerce_app.modules.setting.service.SettingService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

  private final SettingRepository settingRepository;

  @Override
  @Cacheable(value = "settings", key = "#key")
  public String getString(String key) {
    return find(key).getValue();
  }

  @Override
  public Integer getInt(String key) {
    return Integer.parseInt(getString(key));
  }

  @Override
  public BigDecimal getDecimal(String key) {
    return new BigDecimal(getString(key));
  }

  @Override
  @Transactional
  @CacheEvict(value = "settings", key = "#key")
  public SettingDto.Response update(String key, String value) {
    Setting setting = find(key);
    setting.setValue(value);
    return toResponse(settingRepository.save(setting));
  }

  @Override
  @Transactional
  public List<SettingDto.Response> bulkUpdate(List<SettingDto.BulkUpdateItem> items) {
    return items.stream().map(i -> update(i.getKey(), i.getValue())).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<SettingDto.Response> getAll() {
    return settingRepository.findAll().stream().map(this::toResponse).toList();
  }

  private Setting find(String key) {
    return settingRepository
        .findById(key)
        .orElseThrow(() -> new ResourceNotFoundException("Setting", key));
  }

  private SettingDto.Response toResponse(Setting s) {
    return SettingDto.Response.builder()
        .key(s.getKey())
        .value(s.getValue())
        .label(s.getLabel())
        .updatedAt(s.getUpdatedAt())
        .build();
  }
}
