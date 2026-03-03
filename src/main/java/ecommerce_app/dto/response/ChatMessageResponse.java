// response/ChatMessageResponse.java
package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.SenderType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class ChatMessageResponse {
  private Long id;
  private Long senderId;
  private String senderName;
  private String senderAvatar;
  private SenderType senderType;
  private String content;
  private Boolean isRead;
  private LocalDateTime sentAt;
}