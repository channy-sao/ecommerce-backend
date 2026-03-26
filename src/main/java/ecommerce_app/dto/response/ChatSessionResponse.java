package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Chat session response payload")
public class ChatSessionResponse {

  @Schema(description = "Unique session ID", example = "1")
  private Long id;

  @Schema(description = "ID of the customer in this session", example = "42")
  private Long customerId;

  @Schema(description = "Full name of the customer", example = "John Doe")
  private String customerName;

  @Schema(
      description = "Avatar URL of the customer",
      example = "https://example.com/uploads/avatars/john.jpg")
  private String customerAvatar;

  @Schema(description = "ID of the assigned support agent", example = "7")
  private Long agentId;

  @Schema(description = "Full name of the assigned support agent", example = "Jane Smith")
  private String agentName;

  @Schema(description = "Current status of the chat session", example = "OPEN")
  private SessionStatus status;

  @Schema(description = "Number of unread messages in this session", example = "3")
  private long unreadCount;

  @Schema(description = "Date and time the session was created", example = "2024-01-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "Date and time the session was closed", example = "2024-01-01T11:00:00")
  private LocalDateTime closedAt;

  @Schema(description = "List of messages in this session, included only for history endpoint")
  private List<ChatMessageResponse> messages;
}
