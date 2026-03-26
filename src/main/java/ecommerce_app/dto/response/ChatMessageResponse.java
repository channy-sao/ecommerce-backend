package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.SenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Chat message response payload")
public class ChatMessageResponse {

  @Schema(description = "Unique message ID", example = "1")
  private Long id;

  @Schema(description = "ID of the message sender", example = "42")
  private Long senderId;

  @Schema(description = "Full name of the message sender", example = "John Doe")
  private String senderName;

  @Schema(
      description = "Avatar URL of the message sender",
      example = "https://example.com/uploads/avatars/john.jpg")
  private String senderAvatar;

  @Schema(description = "Type of sender (USER or ADMIN)", example = "USER")
  private SenderType senderType;

  @Schema(description = "Message content", example = "Hello, I have a question about my order.")
  private String content;

  @Schema(description = "Whether the message has been read", example = "false")
  private Boolean isRead;

  @Schema(description = "Date and time the message was sent", example = "2024-01-01T10:00:00")
  private LocalDateTime sentAt;
}
