package ecommerce_app.controller;

import ecommerce_app.constant.enums.SenderType;
import ecommerce_app.entity.ChatMessage;
import ecommerce_app.entity.ChatSession;
import ecommerce_app.service.impl.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

  private final ChatService chatService;

  // Customer: load chat history
  @GetMapping("/history/{sessionId}")
  public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable Long sessionId) {
    return ResponseEntity.ok(chatService.getHistory(sessionId));
  }

  // Agent dashboard: see all waiting sessions
  @GetMapping("/sessions/waiting")
  public ResponseEntity<List<ChatSession>> getWaiting() {
    return ResponseEntity.ok(chatService.getWaitingSessions());
  }

  // Mark messages as read
  @PatchMapping("/sessions/{sessionId}/read")
  public ResponseEntity<Void> markRead(
      @PathVariable Long sessionId, @RequestParam SenderType readerType) {
    chatService.markAsRead(sessionId, readerType);
    return ResponseEntity.ok().build();
  }
}
