package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.SenderType;
import ecommerce_app.constant.enums.SessionStatus;
import ecommerce_app.entity.ChatMessage;
import ecommerce_app.entity.ChatSession;
import ecommerce_app.entity.User;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.repository.ChatMessageRepository;
import ecommerce_app.repository.ChatSessionRepository;
import ecommerce_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

  private final ChatSessionRepository sessionRepository;
  private final ChatMessageRepository messageRepository;
  private final UserRepository userRepository;

  public ChatSession createSession(Long customerId) {
    // Check if customer already has an open session
    sessionRepository
        .findByCustomerIdAndStatus(customerId, SessionStatus.WAITING)
        .ifPresent(
            s -> {
              throw new RuntimeException("You already have an open session");
            });

    User customer =
        userRepository
            .findById(customerId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    return sessionRepository.save(
        ChatSession.builder().customer(customer).status(SessionStatus.WAITING).build());
  }

  public ChatSession assignAgent(Long sessionId, Long agentId) {
    ChatSession session = getSessionOrThrow(sessionId);
    User agent =
        userRepository.findById(agentId).orElseThrow(() -> new RuntimeException("Agent not found"));

    session.setAgent(agent);
    session.setStatus(SessionStatus.ACTIVE);
    return sessionRepository.save(session);
  }

  public ChatMessage saveMessage(
      Long sessionId, Long senderId, String content, SenderType senderType) {
    ChatSession session = getSessionOrThrow(sessionId);
    User sender = senderId != null ? userRepository.findById(senderId).orElse(null) : null;

    return messageRepository.save(
        ChatMessage.builder()
            .session(session)
            .sender(sender)
            .senderType(senderType)
            .content(content)
            .isRead(false)
            .build());
  }

  public void closeSession(Long sessionId) {
    ChatSession session = getSessionOrThrow(sessionId);
    session.setStatus(SessionStatus.CLOSED);
    session.setClosedAt(LocalDateTime.now());
    sessionRepository.save(session);
  }

  public List<ChatMessage> getHistory(Long sessionId) {
    return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
  }

  public List<ChatSession> getWaitingSessions() {
    return sessionRepository.findByStatus(SessionStatus.WAITING);
  }

  public long getUnreadCount(Long sessionId, SenderType viewerType) {
    // If agent is viewing → count unread CUSTOMER messages
    // If customer is viewing → count unread AGENT messages
    SenderType opposite = viewerType == SenderType.AGENT ? SenderType.CUSTOMER : SenderType.AGENT;
    return messageRepository.countBySessionIdAndIsReadFalseAndSenderTypeNot(sessionId, viewerType);
  }

  public void markAsRead(Long sessionId, SenderType readerType) {
    SenderType opposite = readerType == SenderType.AGENT ? SenderType.CUSTOMER : SenderType.AGENT;
    messageRepository.markAllAsRead(sessionId, opposite);
  }

  private ChatSession getSessionOrThrow(Long sessionId) {
    return sessionRepository
        .findById(sessionId)
        .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
  }
}
