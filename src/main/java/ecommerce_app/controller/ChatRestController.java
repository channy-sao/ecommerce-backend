// ChatRestController.java
package ecommerce_app.controller;

import ecommerce_app.constant.enums.SenderType;
import ecommerce_app.core.identify.custom.CustomUserDetails;
import ecommerce_app.dto.request.ChatMessageRequest;
import ecommerce_app.dto.request.FcmTokenRequest;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.ChatMessageResponse;
import ecommerce_app.dto.response.ChatSessionResponse;
import ecommerce_app.service.impl.FirebaseChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Controller", description = "For Chat Management")
public class ChatRestController {

  private final FirebaseChatService chatService;

  // ── Sessions ──────────────────────────────────────────────────────────────

  @PostMapping("/sessions/start")
  public ResponseEntity<BaseBodyResponse<ChatSessionResponse>> startChat(
      @AuthenticationPrincipal CustomUserDetails user) {
    return BaseBodyResponse.success(chatService.createSession(user.getId()), "Success");
  }

  @PostMapping("/sessions/{sessionId}/join")
  public ResponseEntity<BaseBodyResponse<ChatSessionResponse>> agentJoin(
      @PathVariable Long sessionId, @AuthenticationPrincipal CustomUserDetails user) {
    return BaseBodyResponse.success(chatService.assignAgent(sessionId, user.getId()), "Success");
  }

  @PostMapping("/sessions/{sessionId}/close")
  public ResponseEntity<BaseBodyResponse<Void>> closeChat(@PathVariable Long sessionId) {
    chatService.closeSession(sessionId);
    return BaseBodyResponse.success("Success");
  }

  @GetMapping("/sessions/waiting")
  public ResponseEntity<BaseBodyResponse<List<ChatSessionResponse>>> getWaiting() {
    return BaseBodyResponse.success(chatService.getWaitingSessions(), "Success");
  }

  @GetMapping("/sessions/my")
  public ResponseEntity<BaseBodyResponse<List<ChatSessionResponse>>> getMySessions(
      @AuthenticationPrincipal CustomUserDetails user) {
    List<ChatSessionResponse> sessions =
        isAgentOrAdmin(user)
            ? chatService.getMySessionsAsAgent(user.getId())
            : chatService.getMySessionsAsCustomer(user.getId());
    return BaseBodyResponse.success(sessions, "Success");
  }

  // ── Messages ──────────────────────────────────────────────────────────────

  @PostMapping("/sessions/{sessionId}/message")
  public ResponseEntity<BaseBodyResponse<ChatMessageResponse>> sendMessage(
      @PathVariable Long sessionId,
      @Valid @RequestBody ChatMessageRequest request,
      @AuthenticationPrincipal CustomUserDetails user) {
    return BaseBodyResponse.success(
        chatService.sendMessage(
            sessionId, user.getId(), request.getContent(), resolveSenderType(user)),
        "");
  }

  @GetMapping("/sessions/{sessionId}/messages")
  public ResponseEntity<BaseBodyResponse<List<ChatMessageResponse>>> getHistory(
      @PathVariable Long sessionId) {
    return BaseBodyResponse.success(chatService.getHistory(sessionId), "Success");
  }

  @PatchMapping("/sessions/{sessionId}/read")
  public ResponseEntity<BaseBodyResponse<Void>> markRead(
      @PathVariable Long sessionId, @AuthenticationPrincipal CustomUserDetails user) {
    chatService.markAsRead(sessionId, resolveSenderType(user));
    return BaseBodyResponse.success("Success");
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private SenderType resolveSenderType(CustomUserDetails user) {
    return isAgentOrAdmin(user) ? SenderType.AGENT : SenderType.CUSTOMER;
  }

  private boolean isAgentOrAdmin(CustomUserDetails user) {
    return user.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(Objects::nonNull)
        .anyMatch(
            r -> r.equals("ROLE_AGENT") || r.equals("ROLE_ADMIN") || r.equals("ROLE_SUPER_ADMIN"));
  }
}
