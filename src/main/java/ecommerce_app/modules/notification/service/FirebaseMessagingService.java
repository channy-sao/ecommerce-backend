package ecommerce_app.modules.notification.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import ecommerce_app.constant.enums.NotificationStatus;
import ecommerce_app.infrastructure.exception.InternalServerErrorException;
import ecommerce_app.modules.notification.model.entity.DeviceToken;
import ecommerce_app.modules.notification.model.entity.Notification;
import ecommerce_app.modules.notification.model.entity.NotificationLog;
import ecommerce_app.modules.notification.repository.NotificationLogRepository;
import ecommerce_app.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Firebase Cloud Messaging Service Handles all FCM operations for sending push notifications */
@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseMessagingService {

  private final FirebaseMessaging firebaseMessaging;
  private final NotificationRepository notificationRepository;
  private final NotificationLogRepository notificationLogRepository;

  /** Send notification to a single device with retry logic */
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
  @Transactional
  public String sendToDevice(
      String token,
      String title,
      String body,
      String imageUrl,
      Map<String, String> data,
      Notification notification,
      DeviceToken deviceToken) {
    try {
      Message.Builder messageBuilder =
          Message.builder()
              .setToken(token)
              .setNotification(
                  com.google.firebase.messaging.Notification.builder()
                      .setTitle(title)
                      .setBody(body)
                      .setImage(imageUrl)
                      .build());

      // Add data payload
      if (data != null && !data.isEmpty()) {
        messageBuilder.putAllData(data);
      }

      // Platform-specific configurations
      messageBuilder.setAndroidConfig(
          AndroidConfig.builder()
              .setPriority(AndroidConfig.Priority.HIGH)
              .setNotification(
                  AndroidNotification.builder()
                      .setSound("default")
                      .setColor("#0066CC")
                      .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                      .build())
              .build());

      messageBuilder.setApnsConfig(
          ApnsConfig.builder()
              .setAps(Aps.builder().setSound("default").setBadge(1).build())
              .build());

      messageBuilder.setWebpushConfig(
          WebpushConfig.builder()
              .setNotification(
                  WebpushNotification.builder()
                      .setTitle(title)
                      .setBody(body)
                      .setIcon("/notification-icon.png")
                      .setBadge("/notification-badge.png")
                      .setImage(imageUrl)
                      .build())
              .build());

      Message message = messageBuilder.build();
      String response = firebaseMessaging.send(message);

      log.info(
          "Successfully sent message to token: {} | FCM Response: {}",
          token.substring(0, 20),
          response);

      // Log success
      logNotificationDelivery(notification, deviceToken, response, "SUCCESS", null);

      // Update notification status
      updateNotificationStatus(notification, NotificationStatus.SENT);

      return response;

    } catch (FirebaseMessagingException e) {
      log.error("Error sending message to token {}: {}", token.substring(0, 20), e.getMessage());

      // Log failure
      logNotificationDelivery(notification, deviceToken, null, "FAILED", e.getMessage());

      // Update notification status
      if (notification != null) {
        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage(e.getMessage());
        notification.setRetryCount(notification.getRetryCount() + 1);
        notificationRepository.save(notification);
      }

      throw new InternalServerErrorException(e.getMessage(), e);
    }
  }

  /** Send notification to multiple devices (multicast) */
  @Transactional
  public BatchResponse sendMulticast(
      List<String> tokens,
      String title,
      String body,
      String imageUrl,
      Map<String, String> data,
      Notification notification) {
    try {
      if (tokens == null || tokens.isEmpty()) {
        log.warn("No tokens provided for multicast");
        return null;
      }

      MulticastMessage.Builder messageBuilder =
          MulticastMessage.builder()
              .addAllTokens(tokens)
              .setNotification(
                  com.google.firebase.messaging.Notification.builder()
                      .setTitle(title)
                      .setBody(body)
                      .setImage(imageUrl)
                      .build());

      // Add data payload
      if (data != null && !data.isEmpty()) {
        messageBuilder.putAllData(data);
      }

      // Platform-specific configs
      messageBuilder.setAndroidConfig(
          AndroidConfig.builder()
              .setPriority(AndroidConfig.Priority.HIGH)
              .setNotification(
                  AndroidNotification.builder().setSound("default").setColor("#0066CC").build())
              .build());

      messageBuilder.setApnsConfig(
          ApnsConfig.builder()
              .setAps(Aps.builder().setSound("default").setBadge(1).build())
              .build());

      MulticastMessage message = messageBuilder.build();
      BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

      log.info(
          "Multicast sent. Success: {} | Failure: {}",
          response.getSuccessCount(),
          response.getFailureCount());

      // Update notification status
      if (response.getSuccessCount() > 0) {
        updateNotificationStatus(notification, NotificationStatus.SENT);
      } else {
        updateNotificationStatus(notification, NotificationStatus.FAILED);
      }

      // Process responses
      processMulticastResponse(response, tokens, notification);

      return response;

    } catch (FirebaseMessagingException e) {
      log.error("Error sending multicast: {}", e.getMessage());

      if (notification != null) {
        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage(e.getMessage());
        notificationRepository.save(notification);
      }

      return null;
    }
  }

  /** Send notification asynchronously */
  public CompletableFuture<String> sendAsync(
      String token,
      String title,
      String body,
      String imageUrl,
      Map<String, String> data,
      Notification notification,
      DeviceToken deviceToken) {
    return CompletableFuture.supplyAsync(
        () -> sendToDevice(token, title, body, imageUrl, data, notification, deviceToken));
  }

  /** Process multicast response and handle failures */
  private void processMulticastResponse(
      BatchResponse response, List<String> tokens, Notification notification) {
    if (response.getFailureCount() > 0) {
      List<SendResponse> responses = response.getResponses();

      for (int i = 0; i < responses.size(); i++) {
        SendResponse sendResponse = responses.get(i);

        if (!sendResponse.isSuccessful()) {
          String token = tokens.get(i);
          String error =
              sendResponse.getException() != null
                  ? sendResponse.getException().getMessage()
                  : "Unknown error";

          log.error("Failed to send to token {}: {}", token.substring(0, 20), error);

          // Log failure
          logNotificationDelivery(notification, null, null, "FAILED", error);
        }
      }
    }
  }

  /** Log notification delivery attempt */
  private void logNotificationDelivery(
      Notification notification,
      DeviceToken deviceToken,
      String fcmMessageId,
      String status,
      String errorMessage) {
    if (notification == null) return;

    try {
      NotificationLog log =
          NotificationLog.builder()
              .notification(notification)
              .deviceToken(deviceToken)
              .fcmMessageId(fcmMessageId)
              .status(status)
              .errorMessage(errorMessage)
              .build();

      notificationLogRepository.save(log);
    } catch (Exception e) {
      log.error("Error logging notification delivery: {}", e.getMessage());
    }
  }

  /** Update notification status in database */
  private void updateNotificationStatus(Notification notification, NotificationStatus status) {
    if (notification != null) {
      notification.setStatus(status);

      if (status == NotificationStatus.SENT) {
        notification.setSentAt(LocalDateTime.now());
      } else if (status == NotificationStatus.DELIVERED) {
        notification.setDeliveredAt(LocalDateTime.now());
      }

      notificationRepository.save(notification);
    }
  }

  /** Build data payload for FCM */
  public Map<String, String> buildDataPayload(Notification notification) {
    Map<String, String> data = new HashMap<>();

    data.put("notificationId", notification.getId().toString());
    data.put("type", notification.getType().name());

    if (notification.getReferenceId() != null) {
      data.put("referenceId", notification.getReferenceId());
    }

    if (notification.getReferenceType() != null) {
      data.put("referenceType", notification.getReferenceType());
    }

    if (notification.getActionUrl() != null) {
      data.put("actionUrl", notification.getActionUrl());
    }

    return data;
  }

  /** Validate FCM token format */
  public boolean isValidToken(String token) {
    return token != null && !token.trim().isEmpty() && token.length() > 20;
  }
}
