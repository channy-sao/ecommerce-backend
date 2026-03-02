package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
  private Long id;
  private Long sessionId;
  private Long senderId;
  private String senderName; // user's full name
  private String senderAvatar; // user's avatar URL
  private SenderType senderType;
  private String content;
  private Boolean isRead;
  private LocalDateTime sentAt;
}
