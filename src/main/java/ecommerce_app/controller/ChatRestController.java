package ecommerce_app.controller;

import ecommerce_app.constant.enums.SenderType;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.request.ChatMessageRequest;
import ecommerce_app.entity.ChatMessage;
import ecommerce_app.entity.ChatSession;
import ecommerce_app.service.impl.FirebaseChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

  private final FirebaseChatService chatService;

  @PostMapping("/sessions/start")
  public ResponseEntity<ChatSession> startChat(@RequestParam Long customerId) {
    return ResponseEntity.ok(chatService.createSession(customerId));
  }

  @PostMapping("/sessions/{sessionId}/join")
  public ResponseEntity<ChatSession> agentJoin(
      @PathVariable Long sessionId, @RequestParam Long agentId) {
    return ResponseEntity.ok(chatService.assignAgent(sessionId, agentId));
  }

  @PostMapping("/sessions/{sessionId}/message")
  public ResponseEntity<ChatMessage> sendMessage(
      @PathVariable Long sessionId,
      @RequestBody ChatMessageRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {
    SenderType senderType = resolveSenderType(user);
    return ResponseEntity.ok(
        chatService.saveMessage(sessionId, user.getId(), request.getContent(), senderType));
  }

  @PostMapping("/sessions/{sessionId}/close")
  public ResponseEntity<Void> closeChat(@PathVariable Long sessionId) {
    chatService.closeSession(sessionId);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/sessions/{sessionId}/read")
  public ResponseEntity<Void> markRead(
      @PathVariable Long sessionId, @RequestParam SenderType readerType) {
    chatService.markAsRead(sessionId, readerType);
    return ResponseEntity.ok().build();
  }

  private SenderType resolveSenderType(CustomUserDetails user) {
    return user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(Objects::nonNull)
            .anyMatch(r -> r.equals("ROLE_AGENT") || r.equals("ROLE_ADMIN"))
        ? SenderType.AGENT
        : SenderType.CUSTOMER;
  }
}
