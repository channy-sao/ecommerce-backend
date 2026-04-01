package ecommerce_app.service.impl;

import ecommerce_app.constant.app.SettingKeys;
import ecommerce_app.dto.response.StoreLocation;
import ecommerce_app.entity.Setting;
import ecommerce_app.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreLocationService {
  private final SettingRepository settingRepository;

  public StoreLocation getStoreLocation() {
    log.info("============= Fetching store location");
    List<Setting> settings =
        settingRepository.findByKeys(
            List.of(
                SettingKeys.STORE_LATITUDE,
                SettingKeys.STORE_LONGITUDE,
                SettingKeys.STORE_ADDRESS,
                SettingKeys.STORE_WEBSITE,
                SettingKeys.STORE_NAME,
                SettingKeys.STORE_PHONE,
                SettingKeys.STORE_EMAIL,
                SettingKeys.STORE_WORKING_HOURS,
                SettingKeys.STORE_LOGO_URL,
                SettingKeys.STORE_CLOSE_AT,
                SettingKeys.STORE_OPEN_AT,
                SettingKeys.STORE_FACEBOOK_URL,
                SettingKeys.STORE_TELEGRAM_URL));

    Map<String, String> settingMap =
        settings.stream().collect(Collectors.toMap(Setting::getKey, Setting::getValue));
    return StoreLocation.builder()
        .phone(settingMap.get(SettingKeys.STORE_PHONE))
        .storeOpenAt(settingMap.get(SettingKeys.STORE_OPEN_AT))
        .storeCloseAt(settingMap.get(SettingKeys.STORE_CLOSE_AT))
        .email(settingMap.get(SettingKeys.STORE_EMAIL))
        .address(settingMap.get(SettingKeys.STORE_ADDRESS))
        .facebook(settingMap.get(SettingKeys.STORE_FACEBOOK_URL))
        .telegram(settingMap.get(SettingKeys.STORE_TELEGRAM_URL))
        .website(settingMap.get(SettingKeys.STORE_WEBSITE))
        .latitude(settingMap.get(SettingKeys.STORE_LATITUDE))
        .longitude(settingMap.get(SettingKeys.STORE_LONGITUDE))
        .build();
  }
}
