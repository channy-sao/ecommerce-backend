package ecommerce_app.api;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.auth.custom.CustomUserDetails;
import ecommerce_app.modules.notification.model.dto.DeviceTokenRequest;
import ecommerce_app.modules.notification.model.dto.NotificationRequest;
import ecommerce_app.modules.notification.model.dto.NotificationResponse;
import ecommerce_app.modules.notification.service.DeviceTokenService;
import ecommerce_app.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

  private final NotificationService notificationService;
  private final DeviceTokenService deviceTokenService;

  /** Create and send notification POST /api/v1/notifications */
  @PostMapping
  public ResponseEntity<BaseBodyResponse<NotificationResponse>> createNotification(
      @Valid @RequestBody NotificationRequest request) {

    NotificationResponse response = notificationService.createAndSendNotification(request);
    return BaseBodyResponse.success(response, "Notification created and sent successfully");
  }

  /** Get user notifications with pagination GET /api/v1/notifications */
  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<NotificationResponse>>> getUserNotifications(
      @AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<NotificationResponse> notifications =
        notificationService.getUserNotifications(userDetails.getId(), pageable);

    return BaseBodyResponse.pageSuccess(notifications, "Notifications retrieved successfully");
  }

  /** Get unread notification count GET /api/v1/notifications/unread-count */
  @GetMapping("/unread-count")
  public ResponseEntity<BaseBodyResponse<Long>> getUnreadCount(
      @AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {
    Long count = notificationService.getUnreadCount(userDetails.getId());
    return BaseBodyResponse.success(count, "Unread count retrieved successfully");
  }

  /** Mark notification as read PUT /api/v1/notifications/{id}/read */
  @PutMapping("/{id}/read")
  public ResponseEntity<BaseBodyResponse<Void>> markAsRead(@PathVariable Long id) {
    notificationService.markAsRead(id);
    return BaseBodyResponse.success("Notification marked as read");
  }

  /** Mark all notifications as read PUT /api/v1/notifications/read-all */
  @PutMapping("/read-all")
  public ResponseEntity<BaseBodyResponse<Void>> markAllAsRead(
      @AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {
    notificationService.markAllAsRead(userDetails.getId());
    return BaseBodyResponse.success("All notifications marked as read");
  }

  /** Register device token POST /api/v1/notifications/device-token */
  @PostMapping("/device-token")
  public ResponseEntity<BaseBodyResponse<Void>> registerDeviceToken(
      @Valid @RequestBody DeviceTokenRequest request,
      @AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {

    deviceTokenService.registerDeviceToken(request, userDetails.getId());
    return BaseBodyResponse.success("Device token registered successfully");
  }

  /**
   * Deactivate device token (logout) DELETE /api/v1/notifications/device-token/{token}/deactivate
   */
  @DeleteMapping("/device-token/{token}/deactivate")
  public ResponseEntity<BaseBodyResponse<Void>> deactivateDeviceToken(@PathVariable String token) {
    deviceTokenService.deactivateDeviceToken(token);
    return BaseBodyResponse.success("Device token deactivated successfully");
  }

  /** Delete device token permanently DELETE /api/v1/notifications/device-token/{token} */
  @DeleteMapping("/device-token/{token}")
  public ResponseEntity<BaseBodyResponse<Void>> deleteDeviceToken(@PathVariable String token) {
    deviceTokenService.deleteDeviceToken(token);
    return BaseBodyResponse.success("Device token deleted successfully");
  }

  /** Send broadcast notification to all users (Admin only) POST /api/v1/notifications/broadcast */
  @PostMapping("/broadcast")
  public ResponseEntity<BaseBodyResponse<Void>> sendBroadcast(
      @Valid @RequestBody NotificationRequest request) {
    notificationService.sendBroadcastNotification(request);
    return BaseBodyResponse.success("Broadcast notification initiated");
  }

  /** Retry failed notifications (Admin only) POST /api/v1/notifications/retry-failed */
  @PostMapping("/retry-failed")
  public ResponseEntity<BaseBodyResponse<Void>> retryFailed() {
    notificationService.retryFailedNotifications();
    return BaseBodyResponse.success("Retry initiated for failed notifications");
  }
}
