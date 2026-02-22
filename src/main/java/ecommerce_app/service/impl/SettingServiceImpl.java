package ecommerce_app.service.impl;

import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.dto.request.BulkUpdateSettingItem;
import ecommerce_app.dto.response.SettingResponse;
import ecommerce_app.entity.Setting;
import ecommerce_app.repository.SettingRepository;
import ecommerce_app.service.SettingService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
    try {
      String value = getString(key);
      if (value == null) {
        log.warn("Setting value is null for key: {}", key);
        return 0; // or throw an exception, or return a default value
      }
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      log.error("Failed to parse setting value to int for key: {}", key, e);
      return 0; // or throw an exception, or return a default value
    }
  }

  @Override
  public BigDecimal getDecimal(String key) {
    try {
      String value = getString(key);
      if (value == null) {
        log.warn("Setting value is null for key: {}", key);
        return BigDecimal.ZERO; // or throw an exception, or return a default value
      }
      return new BigDecimal(value);
    } catch (NumberFormatException e) {
      log.error("Failed to parse setting value to BigDecimal for key: {}", key, e);
      return BigDecimal.ZERO; // or throw an exception, or return a default value
    }
  }

  @Override
  @Transactional
  @CacheEvict(value = "settings", key = "#key")
  public SettingResponse update(String key, String value) {
    Setting setting = find(key);
    setting.setValue(value);
    return toResponse(settingRepository.save(setting));
  }

  @Override
  @Transactional
  public List<SettingResponse> bulkUpdate(List<BulkUpdateSettingItem> items) {
    return items.stream().map(i -> update(i.getKey(), i.getValue())).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<SettingResponse> getAll() {
    return settingRepository.findAll().stream().map(this::toResponse).toList();
  }

  private Setting find(String key) {
    return settingRepository
        .findById(key)
        .orElseThrow(() -> new ResourceNotFoundException("Setting", key));
  }

  private SettingResponse toResponse(Setting s) {
    return SettingResponse.builder()
        .key(s.getKey())
        .value(s.getValue())
        .label(s.getLabel())
        .updatedAt(s.getUpdatedAt())
        .build();
  }
}
