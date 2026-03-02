package ecommerce_app.controller;

import ecommerce_app.constant.enums.SenderType;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.request.ChatMessageRequest;
import ecommerce_app.dto.response.ChatMessageResponse;
import ecommerce_app.dto.response.ChatSessionResponse;
import ecommerce_app.entity.ChatMessage;
import ecommerce_app.entity.ChatSession;
import ecommerce_app.service.impl.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;
  private final SimpMessagingTemplate messagingTemplate;

  // Customer starts a session
  @MessageMapping("/chat.start")
  public void startChat(@Payload Long customerId) {
    ChatSession session = chatService.createSession(customerId);

    // Tell customer their session ID
    messagingTemplate.convertAndSendToUser(
        customerId.toString(), "/queue/session", toSessionResponse(session, 0));

    // Notify all agents of new waiting customer
    messagingTemplate.convertAndSend("/topic/agent/queue", toSessionResponse(session, 0));
  }

  // Agent joins a session
  @MessageMapping("/chat.join/{sessionId}")
  public void agentJoin(@DestinationVariable Long sessionId, @Payload Long agentId) {
    ChatSession session = chatService.assignAgent(sessionId, agentId);

    // System message to the room
    ChatMessage systemMsg =
        chatService.saveMessage(
            sessionId,
            null,
            "Agent " + session.getAgent().getFullName() + " has joined. How can we help you?",
            SenderType.SYSTEM);

    messagingTemplate.convertAndSend("/topic/chat/" + sessionId, toMessageResponse(systemMsg));

    // Update agent dashboard
    messagingTemplate.convertAndSend("/topic/agent/queue", toSessionResponse(session, 0));
  }

  // Send message (customer or agent)
  @MessageMapping("/chat.send/{sessionId}")
  public void sendMessage(
      @DestinationVariable Long sessionId,
      @Payload ChatMessageRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    // Resolve actual user from security context (never trust client-sent ID)
    SenderType senderType = resolveSenderType(userDetails);

    ChatMessage message =
        chatService.saveMessage(sessionId, userDetails.getId(), request.getContent(), senderType);

    // Broadcast to session room (both customer and agent subscribed here)
    messagingTemplate.convertAndSend("/topic/chat/" + sessionId, toMessageResponse(message));
  }

  // Typing indicator (not saved to DB)
  @MessageMapping("/chat.typing/{sessionId}")
  public void typing(@DestinationVariable Long sessionId, @Payload Map<String, Object> payload) {
    messagingTemplate.convertAndSend(
        "/topic/chat/" + sessionId + "/typing", Optional.ofNullable(payload));
  }

  // Close session
  @MessageMapping("/chat.close/{sessionId}")
  public void closeChat(@DestinationVariable Long sessionId) {
    chatService.closeSession(sessionId);

    ChatMessage systemMsg =
        chatService.saveMessage(
            sessionId, null, "Chat session has been closed. Thank you!", SenderType.SYSTEM);

    messagingTemplate.convertAndSend("/topic/chat/" + sessionId, toMessageResponse(systemMsg));
  }

  // Helpers
  private ChatMessageResponse toMessageResponse(ChatMessage msg) {
    return ChatMessageResponse.builder()
        .id(msg.getId())
        .sessionId(msg.getSession().getId())
        .senderId(msg.getSender() != null ? msg.getSender().getId() : null)
        .senderName(msg.getSender() != null ? msg.getSender().getFullName() : "System")
        .senderAvatar(msg.getSender() != null ? msg.getSender().getAvatar() : null)
        .senderType(msg.getSenderType())
        .content(msg.getContent())
        .isRead(msg.getIsRead())
        .sentAt(msg.getCreatedAt()) // from TimeAuditableEntity
        .build();
  }

  private ChatSessionResponse toSessionResponse(ChatSession s, int unread) {
    return ChatSessionResponse.builder()
        .id(s.getId())
        .customerId(s.getCustomer().getId())
        .customerName(s.getCustomer().getFullName())
        .customerAvatar(s.getCustomer().getAvatar())
        .agentId(s.getAgent() != null ? s.getAgent().getId() : null)
        .agentName(s.getAgent() != null ? s.getAgent().getFullName() : null)
        .status(s.getStatus())
        .createdAt(s.getCreatedAt())
        .unreadCount(unread)
        .build();
  }

  private SenderType resolveSenderType(CustomUserDetails userDetails) {

    boolean isAgent =
        userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(Objects::nonNull)
            .anyMatch(role -> role.equals("ROLE_AGENT") || role.equals("ROLE_ADMIN"));

    return isAgent ? SenderType.AGENT : SenderType.CUSTOMER;
  }
}
