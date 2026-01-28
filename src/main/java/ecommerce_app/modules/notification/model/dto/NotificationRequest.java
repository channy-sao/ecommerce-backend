package ecommerce_app.modules.notification.model.dto;

import ecommerce_app.constant.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
  @NotNull(message = "User ID is required")
  private Long userId;

  @NotBlank(message = "Title is required")
  private String title;

  @NotBlank(message = "Message is required")
  private String message;

  @NotNull(message = "Type is required")
  private NotificationType type;

  private String referenceId;
  private String referenceType;
  private String actionUrl;
  private String imageUrl;
  private Map<String, String> data;
  private Boolean sendPush = true;
  private Boolean saveToDatabase = true;
  private Integer expiresInDays;
}
