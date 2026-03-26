package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.NotificationStatus;
import ecommerce_app.constant.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification response payload")
public class NotificationResponse {

  @Schema(description = "Unique notification ID", example = "1")
  private Long id;

  @Schema(description = "ID of the user this notification belongs to", example = "42")
  private Long userId;

  @Schema(description = "Notification title", example = "Order Shipped")
  private String title;

  @Schema(
      description = "Notification message body",
      example = "Your order #ORD-0001 has been shipped.")
  private String message;

  @Schema(description = "Type of notification", example = "ORDER_UPDATE")
  private NotificationType type;

  @Schema(description = "Current status of the notification", example = "DELIVERED")
  private NotificationStatus status;

  @Schema(description = "ID of the referenced entity (e.g. order ID)", example = "101")
  private String referenceId;

  @Schema(description = "Type of the referenced entity", example = "ORDER")
  private String referenceType;

  @Schema(description = "Deep link or URL for the notification action", example = "/orders/101")
  private String actionUrl;

  @Schema(
      description = "Image URL associated with the notification",
      example = "https://cdn.example.com/notifications/shipped.png")
  private String imageUrl;

  @Schema(description = "Whether the notification has been read by the user", example = "false")
  private Boolean isRead;

  @Schema(description = "Whether the notification has been sent", example = "true")
  private Boolean isSent;

  @Schema(description = "Date and time the notification was sent", example = "2024-01-01T10:00:00")
  private LocalDateTime sentAt;

  @Schema(
      description = "Date and time the notification was delivered",
      example = "2024-01-01T10:01:00")
  private LocalDateTime deliveredAt;

  @Schema(description = "Date and time the notification expires", example = "2024-01-08T10:00:00")
  private LocalDateTime expiresAt;

  @Schema(
      description = "Date and time the notification was created",
      example = "2024-01-01T09:59:00")
  private LocalDateTime createdAt;

  @Schema(
      description = "Date and time the notification was read by the user",
      example = "2024-01-01T10:30:00")
  private LocalDateTime readAt;
}
