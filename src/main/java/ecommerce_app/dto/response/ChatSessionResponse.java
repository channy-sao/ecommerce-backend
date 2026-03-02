package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResponse {
  private Long id;
  private Long customerId;
  private String customerName;
  private String customerAvatar;
  private Long agentId;
  private String agentName;
  private SessionStatus status;
  private LocalDateTime createdAt;
  private int unreadCount;
}
