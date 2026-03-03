// response/ChatSessionResponse.java
package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.SessionStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class ChatSessionResponse {
  private Long id;
  private Long customerId;
  private String customerName;
  private String customerAvatar;
  private Long agentId;
  private String agentName;
  private SessionStatus status;
  private long unreadCount;
  private LocalDateTime createdAt;
  private LocalDateTime closedAt;
  private List<ChatMessageResponse> messages; // optional — for history endpoint
}