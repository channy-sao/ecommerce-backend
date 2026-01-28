package ecommerce_app.api;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.notification.model.dto.DeviceTokenRequest;
import ecommerce_app.modules.notification.model.dto.NotificationRequest;
import ecommerce_app.modules.notification.model.dto.NotificationResponse;
import ecommerce_app.modules.notification.service.DeviceTokenService;
import ecommerce_app.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

  private final NotificationService notificationService;
  private final DeviceTokenService deviceTokenService;

  /** Create and send notification POST /api/v1/notifications */
  @PostMapping
  public ResponseEntity<BaseBodyResponse> createNotification(
      @Valid @RequestBody NotificationRequest request) {

    NotificationResponse response = notificationService.createAndSendNotification(request);
    return BaseBodyResponse.success(response, "Notification created and sent successfully");
  }

  /** Get user notifications with pagination GET /api/v1/notifications/user/{userId} */
  @GetMapping("/user/{userId}")
  public ResponseEntity<BaseBodyResponse> getUserNotifications(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<NotificationResponse> notifications =
        notificationService.getUserNotifications(userId, pageable);

    return BaseBodyResponse.success(notifications, "Notifications retrieved successfully");
  }

  /** Get unread notification count GET /api/v1/notifications/user/{userId}/unread-count */
  @GetMapping("/user/{userId}/unread-count")
  public ResponseEntity<BaseBodyResponse> getUnreadCount(@PathVariable Long userId) {
    Long count = notificationService.getUnreadCount(userId);
    return BaseBodyResponse.success(count, "Unread count retrieved successfully");
  }

  /** Mark notification as read PUT /api/v1/notifications/{id}/read */
  @PutMapping("/{id}/read")
  public ResponseEntity<BaseBodyResponse> markAsRead(@PathVariable Long id) {
    notificationService.markAsRead(id);
    return BaseBodyResponse.success(null, "Notification marked as read");
  }

  /** Mark all notifications as read PUT /api/v1/notifications/user/{userId}/read-all */
  @PutMapping("/user/{userId}/read-all")
  public ResponseEntity<BaseBodyResponse> markAllAsRead(@PathVariable Long userId) {
    notificationService.markAllAsRead(userId);
    return BaseBodyResponse.success(null, "All notifications marked as read");
  }

  /** Register device token POST /api/v1/notifications/device-token */
  @PostMapping("/device-token")
  public ResponseEntity<BaseBodyResponse> registerDeviceToken(
      @Valid @RequestBody DeviceTokenRequest request) {

    deviceTokenService.registerDeviceToken(request);
    return BaseBodyResponse.success(null, "Device token registered successfully");
  }

  /**
   * Deactivate device token (logout) DELETE /api/v1/notifications/device-token/{token}/deactivate
   */
  @DeleteMapping("/device-token/{token}/deactivate")
  public ResponseEntity<BaseBodyResponse> deactivateDeviceToken(@PathVariable String token) {
    deviceTokenService.deactivateDeviceToken(token);
    return BaseBodyResponse.success(null, "Device token deactivated successfully");
  }

  /** Delete device token permanently DELETE /api/v1/notifications/device-token/{token} */
  @DeleteMapping("/device-token/{token}")
  public ResponseEntity<BaseBodyResponse> deleteDeviceToken(@PathVariable String token) {
    deviceTokenService.deleteDeviceToken(token);
    return BaseBodyResponse.success(null, "Device token deleted successfully");
  }

  /** Send broadcast notification to all users (Admin only) POST /api/v1/notifications/broadcast */
  @PostMapping("/broadcast")
  public ResponseEntity<BaseBodyResponse> sendBroadcast(
      @Valid @RequestBody NotificationRequest request) {
    notificationService.sendBroadcastNotification(request);
    return BaseBodyResponse.success(null, "Broadcast notification initiated");
  }

  /** Retry failed notifications (Admin only) POST /api/v1/notifications/retry-failed */
  @PostMapping("/retry-failed")
  public ResponseEntity<BaseBodyResponse> retryFailed() {
    notificationService.retryFailedNotifications();
    return BaseBodyResponse.success(null, "Retry initiated for failed notifications");
  }
}
