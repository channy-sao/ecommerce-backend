package ecommerce_app.modules.notification.model.dto;

import ecommerce_app.constant.enums.NotificationStatus;
import ecommerce_app.constant.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
  private Long id;
  private Long userId;
  private String title;
  private String message;
  private NotificationType type;
  private NotificationStatus status;
  private String referenceId;
  private String referenceType;
  private String actionUrl;
  private String imageUrl;
  private Boolean isRead;
  private Boolean isSent;
  private LocalDateTime sentAt;
  private LocalDateTime deliveredAt;
  private LocalDateTime expiresAt;
  private LocalDateTime createdAt;
  private LocalDateTime readAt;
}
