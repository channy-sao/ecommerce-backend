package ecommerce_app.modules.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecommerce_app.constant.enums.NotificationStatus;
import ecommerce_app.infrastructure.exception.InternalServerErrorException;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.modules.notification.model.dto.NotificationRequest;
import ecommerce_app.modules.notification.model.dto.NotificationResponse;
import ecommerce_app.modules.notification.model.entity.DeviceToken;
import ecommerce_app.modules.notification.model.entity.Notification;
import ecommerce_app.modules.notification.repository.DeviceTokenRepository;
import ecommerce_app.modules.notification.repository.NotificationRepository;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final DeviceTokenRepository deviceTokenRepository;
  private final UserRepository userRepository;
  private final FirebaseMessagingService firebaseMessagingService;
  private final ObjectMapper objectMapper;

  private static final int BROADCAST_BATCH_SIZE = 100;
  private static final int MAX_RETRY_COUNT = 3;
  private static final int CLEANUP_DAYS = 30;
  private static final int INACTIVE_TOKEN_DAYS = 90;

  /**
   * MAIN FLOW: Create notification and send via Firebase
   *
   * <p>Step 1: Validate user and request Step 2: Save to database (if requested) Step 3: Send push
   * notification via Firebase (async) Step 4: Return response
   */
  @Transactional
  public NotificationResponse createAndSendNotification(NotificationRequest request) {
    log.info(
        "Creating notification for user: {} with type: {}", request.getUserId(), request.getType());

    // Step 1: Validate user exists
    User user =
        userRepository
            .findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User" + request.getUserId()));

    // Validate user is active
    if (Boolean.FALSE.equals(user.getIsActive())) {
      throw new IllegalStateException("Cannot send notification to inactive user: " + user.getId());
    }

    // Step 2: Save notification to database (if requested)
    Notification notification = null;
    if (Boolean.TRUE.equals(request.getSaveToDatabase())) {
      notification = saveNotificationToDatabase(user, request);
      log.info("Notification saved to database with ID: {}", notification.getId());
    }

    // Step 3: Send push notification via Firebase asynchronously (if requested)
    if (Boolean.TRUE.equals(request.getSendPush())) {
      Notification notificationToSend =
          notification != null ? notification : buildTransientNotification(user, request);
      // Send async to avoid blocking
      sendPushNotificationAsync(notificationToSend);
    }

    // Step 4: Return response
    return notification != null
        ? mapToResponse(notification)
        : buildTransientResponse(user, request);
  }

  /** Save notification to database with validation */
  private Notification saveNotificationToDatabase(User user, NotificationRequest request) {
    try {
      // Validate required fields
      validateNotificationRequest(request);

      Notification notification =
          Notification.builder()
              .user(user)
              .title(request.getTitle())
              .message(request.getMessage())
              .type(request.getType())
              .status(NotificationStatus.PENDING)
              .referenceId(request.getReferenceId())
              .referenceType(request.getReferenceType())
              .actionUrl(request.getActionUrl())
              .imageUrl(request.getImageUrl())
              .data(
                  request.getData() != null
                      ? objectMapper.writeValueAsString(request.getData())
                      : null)
              .isRead(false)
              .isSent(false)
              .retryCount(0)
              .build();

      // Set expiration if specified
      if (request.getExpiresInDays() != null && request.getExpiresInDays() > 0) {
        notification.setExpiresAt(LocalDateTime.now().plusDays(request.getExpiresInDays()));
      }

      return notificationRepository.save(notification);

    } catch (Exception e) {
      log.error("Error serializing notification data: {}", e.getMessage(), e);
      throw new InternalServerErrorException("Failed to save notification", e);
    }
  }

  /** Validate notification request */
  private void validateNotificationRequest(NotificationRequest request) {
    if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Notification title is required");
    }
    if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
      throw new IllegalArgumentException("Notification message is required");
    }
    if (request.getTitle().length() > 255) {
      throw new IllegalArgumentException("Notification title too long (max 255 characters)");
    }
    if (request.getMessage().length() > 1000) {
      throw new IllegalArgumentException("Notification message too long (max 1000 characters)");
    }
  }

  /** Build transient notification (not saved to DB) */
  private Notification buildTransientNotification(User user, NotificationRequest request) {
    return Notification.builder()
        .user(user)
        .title(request.getTitle())
        .message(request.getMessage())
        .type(request.getType())
        .referenceId(request.getReferenceId())
        .referenceType(request.getReferenceType())
        .actionUrl(request.getActionUrl())
        .imageUrl(request.getImageUrl())
        .status(NotificationStatus.PENDING)
        .build();
  }

  /**
   * Send push notification asynchronously Runs in separate thread to avoid blocking main
   * transaction
   */
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendPushNotificationAsync(Notification notification) {
    sendPushNotification(notification);
  }

  /**
   * Send push notification via Firebase Gets all active device tokens and sends notification to
   * each
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendPushNotification(Notification notification) {
    try {
      log.info(
          "Sending push notification for notification ID: {}",
          notification.getId() != null ? notification.getId() : "transient");

      // Get all active device tokens for user
      List<DeviceToken> tokens =
          deviceTokenRepository.findByUserAndIsActiveTrue(notification.getUser());

      if (tokens.isEmpty()) {
        log.warn("No active device tokens found for user: {}", notification.getUser().getEmail());
        updateNotificationStatus(
            notification, NotificationStatus.FAILED, "No active device tokens");
        return;
      }

      log.info(
          "Found {} active device token(s) for user: {}",
          tokens.size(),
          notification.getUser().getEmail());

      // Build FCM data payload
      Map<String, String> data = firebaseMessagingService.buildDataPayload(notification);

      // Send to all devices
      if (tokens.size() == 1) {
        // Single device
        DeviceToken deviceToken = tokens.getFirst();
        sendToSingleDevice(notification, deviceToken, data);
      } else {
        // Multiple devices - use multicast
        sendToMultipleDevices(notification, tokens, data);
      }

    } catch (Exception e) {
      log.error("Error sending push notification: {}", e.getMessage(), e);
      updateNotificationStatus(notification, NotificationStatus.FAILED, e.getMessage());
    }
  }

  /** Send notification to single device */
  private void sendToSingleDevice(
      Notification notification, DeviceToken deviceToken, Map<String, String> data) {
    try {
      String response =
          firebaseMessagingService.sendToDevice(
              deviceToken.getToken(),
              notification.getTitle(),
              notification.getMessage(),
              notification.getImageUrl(),
              data,
              notification,
              deviceToken);

      log.info("Push notification sent successfully. FCM Response: {}", response);

      if (notification.getId() != null) {
        updateNotificationStatus(notification, NotificationStatus.SENT, null);
      }

    } catch (Exception e) {
      log.error("Failed to send to device: {}", e.getMessage());
      updateNotificationStatus(notification, NotificationStatus.FAILED, e.getMessage());
    }
  }

  /** Send notification to multiple devices using multicast */
  private void sendToMultipleDevices(
      Notification notification, List<DeviceToken> tokens, Map<String, String> data) {
    try {
      List<String> tokenStrings = tokens.stream().map(DeviceToken::getToken).toList();

      firebaseMessagingService.sendMulticast(
          tokenStrings,
          notification.getTitle(),
          notification.getMessage(),
          notification.getImageUrl(),
          data,
          notification);

      log.info("Multicast push notification sent to {} devices", tokens.size());

    } catch (Exception e) {
      log.error("Failed to send multicast: {}", e.getMessage());
      updateNotificationStatus(notification, NotificationStatus.FAILED, e.getMessage());
    }
  }

  /**
   * Update notification status in database Uses separate transaction to ensure status is saved even
   * if parent transaction fails
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateNotificationStatus(
      Notification notification, NotificationStatus status, String errorMessage) {
    if (notification == null || notification.getId() == null) return;

    try {
      // Re-fetch to avoid detached entity issues
      Notification dbNotification =
          notificationRepository.findById(notification.getId()).orElse(notification);

      dbNotification.setStatus(status);

      if (status == NotificationStatus.SENT) {
        dbNotification.setIsSent(true);
        dbNotification.setSentAt(LocalDateTime.now());
      } else if (status == NotificationStatus.DELIVERED) {
        dbNotification.setDeliveredAt(LocalDateTime.now());
        dbNotification.setIsSent(true);
      }

      if (errorMessage != null) {
        dbNotification.setErrorMessage(errorMessage);
        dbNotification.setRetryCount(dbNotification.getRetryCount() + 1);
      }

      notificationRepository.save(dbNotification);
      log.debug("Updated notification {} status to {}", notification.getId(), status);

    } catch (Exception e) {
      log.error("Error updating notification status: {}", e.getMessage(), e);
    }
  }

  /** Get user notifications with pagination */
  @Transactional(readOnly = true)
  public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User" + userId));

    return notificationRepository
        .findByUserAndIsDeletedFalseOrderByCreatedAtDesc(user, pageable)
        .map(this::mapToResponse);
  }

  /** Get unread notifications count */
  @Transactional(readOnly = true)
  public Long getUnreadCount(Long userId) {
    // Use optimized query with userId directly
    return notificationRepository.countUnreadByUserId(userId);
  }

  /** Mark notification as read */
  @Transactional
  public void markAsRead(Long notificationId) {
    // Use optimized update query instead of fetch + save
    int updated =
        notificationRepository.markAsRead(
            notificationId, LocalDateTime.now(), NotificationStatus.READ);

    if (updated == 0) {
      throw new ResourceNotFoundException("Notification" + notificationId);
    }

    log.info("Notification marked as read: {}", notificationId);
  }

  /** Mark all notifications as read for user */
  @Transactional
  public void markAllAsRead(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User" + userId));

    int updated =
        notificationRepository.markAllAsRead(user, LocalDateTime.now(), NotificationStatus.READ);

    log.info("Marked {} notifications as read for user: {}", updated, userId);
  }

  /** Delete notification (soft delete) */
  @Transactional
  public void deleteNotification(Long notificationId, Long userId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification" + notificationId));

    // Verify ownership
    if (!notification.getUser().getId().equals(userId)) {
      throw new IllegalArgumentException("Cannot delete notification belonging to another user");
    }

    notification.setIsDeleted(true);
    notification.setDeletedAt(LocalDateTime.now());
    notificationRepository.save(notification);

    log.info("Notification {} deleted by user {}", notificationId, userId);
  }

  /** Send broadcast notification to all users */
  @Async
  @Transactional
  public CompletableFuture<Void> sendBroadcastNotification(NotificationRequest request) {
    log.info("Starting broadcast notification: {}", request.getTitle());

    long totalUsers = userRepository.count();
    int totalPages = (int) Math.ceil((double) totalUsers / BROADCAST_BATCH_SIZE);

    log.info("Broadcasting to {} users in {} batches", totalUsers, totalPages);

    int successCount = 0;
    int failureCount = 0;

    for (int page = 0; page < totalPages; page++) {
      Pageable pageable = PageRequest.of(page, BROADCAST_BATCH_SIZE);
      Page<User> userPage = userRepository.findAll(pageable);

      int[] results = processBroadcastBatch(userPage.getContent(), request);
      successCount += results[0];
      failureCount += results[1];

      log.info(
          "Processed batch {}/{} - Success: {}, Failed: {}",
          page + 1,
          totalPages,
          results[0],
          results[1]);
    }

    log.info(
        "Broadcast notification completed - Total Success: {}, Total Failed: {}",
        successCount,
        failureCount);

    return CompletableFuture.completedFuture(null);
  }

  /** Process batch of users for broadcast Returns [successCount, failureCount] */
  private int[] processBroadcastBatch(List<User> users, NotificationRequest request) {
    int successCount = 0;
    int failureCount = 0;

    for (User user : users) {
      try {
        // Skip inactive users
        if (Boolean.FALSE.equals(user.getIsActive())) {
          continue;
        }

        NotificationRequest userRequest =
            NotificationRequest.builder()
                .userId(user.getId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .actionUrl(request.getActionUrl())
                .imageUrl(request.getImageUrl())
                .data(request.getData())
                .sendPush(request.getSendPush())
                .saveToDatabase(request.getSaveToDatabase())
                .expiresInDays(request.getExpiresInDays())
                .build();

        createAndSendNotification(userRequest);
        successCount++;

      } catch (Exception e) {
        log.error("Failed to send broadcast to user {}: {}", user.getId(), e.getMessage());
        failureCount++;
      }
    }

    return new int[] {successCount, failureCount};
  }

  /** Retry failed notifications Scheduled to run every 5 minutes */
  @Scheduled(fixedDelay = 300000, initialDelay = 60000)
  @Transactional
  public void retryFailedNotifications() {
    List<Notification> failedNotifications =
        notificationRepository.findPendingForRetry(NotificationStatus.FAILED, MAX_RETRY_COUNT);

    if (failedNotifications.isEmpty()) {
      return;
    }

    log.info("Retrying {} failed notifications", failedNotifications.size());

    int successCount = 0;
    int failureCount = 0;

    for (Notification notification : failedNotifications) {
      try {
        sendPushNotification(notification);
        successCount++;
      } catch (Exception e) {
        log.error("Retry failed for notification {}: {}", notification.getId(), e.getMessage());
        failureCount++;
      }
    }

    log.info("Retry completed - Success: {}, Failed: {}", successCount, failureCount);
  }

  /** Clean up old read notifications Scheduled to run daily at 2 AM */
  @Scheduled(cron = "0 0 2 * * *")
  @Transactional
  public void cleanupOldNotifications() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(CLEANUP_DAYS);
    int deleted = notificationRepository.deleteOldReadNotifications(cutoffDate);

    log.info("Cleaned up {} old read notifications (older than {} days)", deleted, CLEANUP_DAYS);
  }

  /** Delete expired notifications Scheduled to run daily at 3 AM */
  @Scheduled(cron = "0 0 3 * * *")
  @Transactional
  public void deleteExpiredNotifications() {
    int deleted = notificationRepository.deleteExpiredNotifications(LocalDateTime.now());

    log.info("Deleted {} expired notifications", deleted);
  }

  /** Deactivate inactive device tokens Scheduled to run daily at 4 AM */
  @Scheduled(cron = "0 0 4 * * *")
  @Transactional
  public void deactivateInactiveTokens() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(INACTIVE_TOKEN_DAYS);
    int deactivated = deviceTokenRepository.deactivateOldTokens(cutoffDate);

    log.info(
        "Deactivated {} inactive device tokens (inactive for {} days)",
        deactivated,
        INACTIVE_TOKEN_DAYS);
  }

  /** Get notification statistics for user */
  @Transactional(readOnly = true)
  public Map<String, Long> getNotificationStats(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    Object[] stats = notificationRepository.getNotificationStats(user);

    return Map.of(
        "total", (Long) stats[0],
        "unread", (Long) stats[1],
        "sent", (Long) stats[2],
        "failed", (Long) stats[3]);
  }

  /** Map entity to response DTO */
  private NotificationResponse mapToResponse(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .userId(notification.getUser().getId())
        .title(notification.getTitle())
        .message(notification.getMessage())
        .type(notification.getType())
        .status(notification.getStatus())
        .referenceId(notification.getReferenceId())
        .referenceType(notification.getReferenceType())
        .actionUrl(notification.getActionUrl())
        .imageUrl(notification.getImageUrl())
        .isRead(notification.getIsRead())
        .isSent(notification.getIsSent())
        .readAt(notification.getReadAt())
        .sentAt(notification.getSentAt())
        .deliveredAt(notification.getDeliveredAt())
        .createdAt(notification.getCreatedAt())
        .expiresAt(notification.getExpiresAt())
        .build();
  }

  /** Build transient response (for notifications not saved to DB) */
  private NotificationResponse buildTransientResponse(User user, NotificationRequest request) {
    return NotificationResponse.builder()
        .userId(user.getId())
        .title(request.getTitle())
        .message(request.getMessage())
        .type(request.getType())
        .status(NotificationStatus.PENDING)
        .referenceId(request.getReferenceId())
        .referenceType(request.getReferenceType())
        .actionUrl(request.getActionUrl())
        .imageUrl(request.getImageUrl())
        .isRead(false)
        .createdAt(LocalDateTime.now())
        .build();
  }
}
