package ecommerce_app.modules.notification.service;

import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.notification.model.dto.DeviceTokenRequest;
import ecommerce_app.modules.notification.model.entity.DeviceToken;
import ecommerce_app.modules.notification.repository.DeviceTokenRepository;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Device Token Service Manages FCM device tokens for push notifications */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenService {

  private final DeviceTokenRepository deviceTokenRepository;
  private final UserRepository userRepository;

  /** Register or update device token */
  @Transactional
  public void registerDeviceToken(DeviceTokenRequest request, Long userId) {
    log.info("Registering device token for user: {}", userId);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    deviceTokenRepository
        .findByToken(request.getToken())
        .ifPresentOrElse(
            existingToken -> updateExistingToken(existingToken, user, request),
            () -> createNewToken(user, request));
  }

  /** Update existing device token */
  private void updateExistingToken(
      DeviceToken existingToken, User user, DeviceTokenRequest request) {
    existingToken.setUser(user);
    existingToken.setDeviceType(request.getDeviceType());
    existingToken.setDeviceName(request.getDeviceName());
    existingToken.setDeviceModel(request.getDeviceModel());
    existingToken.setOsVersion(request.getOsVersion());
    existingToken.setAppVersion(request.getAppVersion());
    existingToken.setIsActive(true);
    existingToken.setLastUsedAt(LocalDateTime.now());

    deviceTokenRepository.save(existingToken);
    log.info("Device token updated for user: {}", user.getId());
  }

  /** Create new device token */
  private void createNewToken(User user, DeviceTokenRequest request) {
    DeviceToken newToken =
        DeviceToken.builder()
            .user(user)
            .token(request.getToken())
            .deviceType(request.getDeviceType())
            .deviceName(request.getDeviceName())
            .deviceModel(request.getDeviceModel())
            .osVersion(request.getOsVersion())
            .appVersion(request.getAppVersion())
            .isActive(true)
            .lastUsedAt(LocalDateTime.now())
            .build();

    deviceTokenRepository.save(newToken);
    log.info("New device token created for user: {}", user.getId());
  }

  /** Deactivate device token (on logout) */
  @Transactional
  public void deactivateDeviceToken(String token) {
    deviceTokenRepository
        .findByToken(token)
        .ifPresent(
            deviceToken -> {
              deviceToken.setIsActive(false);
              deviceTokenRepository.save(deviceToken);
              log.info("Device token deactivated");
            });
  }

  /** Delete device token permanently */
  @Transactional
  public void deleteDeviceToken(String token) {
    deviceTokenRepository.deleteByToken(token);
    log.info("Device token deleted: {}", token.substring(0, Math.min(20, token.length())));
  }

  /** Get active tokens for user */
  @Transactional(readOnly = true)
  public List<DeviceToken> getActiveTokensForUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    return deviceTokenRepository.findByUserAndIsActiveTrue(user);
  }

  /** Deactivate old tokens (scheduled task) */
  @Transactional
  public int deactivateOldTokens(int daysInactive) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
    int count = deviceTokenRepository.deactivateOldTokens(cutoffDate);

    log.info("Deactivated {} old device tokens", count);
    return count;
  }
}
