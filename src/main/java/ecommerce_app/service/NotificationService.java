package ecommerce_app.service;

import ecommerce_app.dto.request.NotificationRequest;
import ecommerce_app.dto.response.NotificationResponse;
import ecommerce_app.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Service interface for managing notifications */
public interface NotificationService {

  // ────────────────────────────────────────────────────────────────────────────
  // Core Operations
  // ────────────────────────────────────────────────────────────────────────────

  NotificationResponse createAndSendNotification(NotificationRequest request);

  void sendPushNotificationAsync(Notification notification);

  void sendPushNotification(Notification notification);

  // ────────────────────────────────────────────────────────────────────────────
  // Query Operations
  // ────────────────────────────────────────────────────────────────────────────

  Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

  Long getUnreadCount(Long userId);

  Map<String, Long> getNotificationStats(Long userId);

  // ────────────────────────────────────────────────────────────────────────────
  // Update Operations
  // ────────────────────────────────────────────────────────────────────────────

  void markAsRead(Long notificationId);

  void markAllAsRead(Long userId);

  void deleteNotification(Long notificationId, Long userId);

  // ────────────────────────────────────────────────────────────────────────────
  // Broadcast Operations
  // ────────────────────────────────────────────────────────────────────────────

  CompletableFuture<Void> sendBroadcastNotification(NotificationRequest request);

  // ────────────────────────────────────────────────────────────────────────────
  // Scheduled Maintenance Operations
  // ────────────────────────────────────────────────────────────────────────────

  void retryFailedNotifications();

  void cleanupOldNotifications();

  void deleteExpiredNotifications();

  void deactivateInactiveTokens();
}
