// FirebaseChatService.java
package ecommerce_app.service.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.messaging.*;
import ecommerce_app.constant.enums.SenderType;
import ecommerce_app.constant.enums.SessionStatus;
import ecommerce_app.dto.response.ChatMessageResponse;
import ecommerce_app.dto.response.ChatSessionResponse;
import ecommerce_app.entity.ChatMessage;
import ecommerce_app.entity.ChatSession;
import ecommerce_app.entity.DeviceToken;
import ecommerce_app.entity.User;
import ecommerce_app.exception.ApiException;
import ecommerce_app.exception.InternalServerErrorException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.ChatMapper;
import ecommerce_app.repository.ChatMessageRepository;
import ecommerce_app.repository.ChatSessionRepository;
import ecommerce_app.repository.DeviceTokenRepository;
import ecommerce_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseChatService {

  private final Firestore firestore;
  private final FirebaseMessaging firebaseMessaging;
  private final UserRepository userRepository;
  private final ChatSessionRepository sessionRepository;
  private final ChatMessageRepository messageRepository;
  private final DeviceTokenRepository deviceTokenRepository;
  private final ChatMapper chatMapper;

  private static final String SESSIONS_COL = "chat_sessions";
  private static final String MESSAGES_COL = "messages";

  // ── Sessions ──────────────────────────────────────────────────────────────

  @Transactional
  public ChatSessionResponse createSession(Long customerId) {
    boolean hasOpen =
        sessionRepository.existsByCustomerIdAndStatusIn(
            customerId, List.of(SessionStatus.WAITING, SessionStatus.ACTIVE));
    if (hasOpen) {
      throw new ApiException(HttpStatus.MULTI_STATUS, "You already have an open support session.");
    }

    User customer =
        userRepository
            .findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + customerId));

    ChatSession session =
        sessionRepository.save(
            ChatSession.builder().customer(customer).status(SessionStatus.WAITING).build());

    syncSessionToFirestore(session, true);
    return chatMapper.toSessionResponse(session, 0);
  }

  @Transactional
  public ChatSessionResponse assignAgent(Long sessionId, Long agentId) {
    ChatSession session = getSessionOrThrow(sessionId);
    if (session.getStatus() == SessionStatus.CLOSED) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot join a closed session.");
    }

    User agent =
        userRepository
            .findById(agentId)
            .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentId));

    session.setAgent(agent);
    session.setStatus(SessionStatus.ACTIVE);
    sessionRepository.save(session);
    syncSessionToFirestore(session, false);

    internalSaveMessage(
        session,
        null,
        "Agent " + agent.getFullName() + " has joined. How can I help you today?",
        SenderType.SYSTEM);

    return chatMapper.toSessionResponse(session, 0);
  }

  @Transactional
  public void closeSession(Long sessionId) {
    ChatSession session = getSessionOrThrow(sessionId);
    if (session.getStatus() == SessionStatus.CLOSED) {
      throw new InternalServerErrorException("Session is already closed.");
    }

    session.setStatus(SessionStatus.CLOSED);
    session.setClosedAt(LocalDateTime.now());
    sessionRepository.save(session);

    internalSaveMessage(
        session,
        null,
        "This chat has been closed. Thank you for contacting us!",
        SenderType.SYSTEM);

    firestore
        .collection(SESSIONS_COL)
        .document(sessionId.toString())
        .update("status", "CLOSED", "closedAt", Timestamp.now());
  }

  // ── Messages ──────────────────────────────────────────────────────────────

  @Transactional
  public ChatMessageResponse sendMessage(
      Long sessionId, Long senderId, String content, SenderType senderType) {
    ChatSession session = getSessionOrThrow(sessionId);

    if (session.getStatus() == SessionStatus.CLOSED) {
      throw new InternalServerErrorException("Cannot send messages to a closed session.");
    }

    User sender =
        userRepository
            .findById(senderId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + senderId));

    ChatMessage message = internalSaveMessage(session, sender, content, senderType);

    // Atomically increment unread count
    firestore
        .collection(SESSIONS_COL)
        .document(sessionId.toString())
        .update(
            "unreadCount", FieldValue.increment(1),
            "lastMessage", content,
            "lastMessageAt", Timestamp.now());

    return chatMapper.toMessageResponse(message);
  }

  @Transactional
  public void markAsRead(Long sessionId, SenderType readerType) {
    SenderType opposite = readerType == SenderType.AGENT ? SenderType.CUSTOMER : SenderType.AGENT;

    messageRepository.markAllAsRead(sessionId, opposite);

    CollectionReference msgCol =
        firestore.collection(SESSIONS_COL).document(sessionId.toString()).collection(MESSAGES_COL);

    // ✅ Fixed: actually batch-update Firestore docs
    msgCol
        .whereEqualTo("senderType", opposite.name())
        .whereEqualTo("isRead", false)
        .get()
        .addListener(
            () -> {
              try {
                QuerySnapshot snap =
                    msgCol
                        .whereEqualTo("senderType", opposite.name())
                        .whereEqualTo("isRead", false)
                        .get()
                        .get();

                if (snap.isEmpty()) return;

                WriteBatch batch = firestore.batch();
                snap.getDocuments()
                    .forEach(doc -> batch.update(doc.getReference(), "isRead", true));
                batch.commit();

                firestore
                    .collection(SESSIONS_COL)
                    .document(sessionId.toString())
                    .update("unreadCount", 0);

              } catch (Exception e) {
                log.error("Firestore markAsRead failed for session {}", sessionId, e);
              }
            },
            Runnable::run);
  }

  // ── Queries ───────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getHistory(Long sessionId) {
    getSessionOrThrow(sessionId);
    return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
        .map(chatMapper::toMessageResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ChatSessionResponse> getWaitingSessions() {
    return sessionRepository.findByStatusOrderByCreatedAtAsc(SessionStatus.WAITING).stream()
        .map(s -> chatMapper.toSessionResponse(s, countUnread(s.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ChatSessionResponse> getMySessionsAsCustomer(Long customerId) {
    return sessionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
        .map(s -> chatMapper.toSessionResponse(s, countUnread(s.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ChatSessionResponse> getMySessionsAsAgent(Long agentId) {
    return sessionRepository.findByAgentIdOrderByCreatedAtDesc(agentId).stream()
        .map(s -> chatMapper.toSessionResponse(s, countUnread(s.getId())))
        .toList();
  }

  // ── Internal ──────────────────────────────────────────────────────────────

  private ChatMessage internalSaveMessage(
      ChatSession session, User sender, String content, SenderType senderType) {
    ChatMessage message =
        messageRepository.save(
            ChatMessage.builder()
                .session(session)
                .sender(sender)
                .senderType(senderType)
                .content(content)
                .isRead(false)
                .build());

    Map<String, Object> doc = new HashMap<>();
    doc.put("id", message.getId());
    doc.put("senderId", sender != null ? sender.getId() : null);
    doc.put("senderName", sender != null ? sender.getFullName() : "System");
    doc.put("senderType", senderType.name());
    doc.put("content", content);
    doc.put("isRead", false);
    doc.put("sentAt", Timestamp.now());

    firestore
        .collection(SESSIONS_COL)
        .document(session.getId().toString())
        .collection(MESSAGES_COL)
        .document(message.getId().toString())
        .set(doc);

    sendPushNotification(session, sender, content, senderType);
    return message;
  }

  private void sendPushNotification(
          ChatSession session,
          User sender,
          String content,
          SenderType senderType) {

    User recipient =
            senderType == SenderType.CUSTOMER
                    ? session.getAgent()
                    : session.getCustomer();

    if (recipient == null) return;

    List<DeviceToken> activeTokens =
            deviceTokenRepository.findByUserIdAndIsActiveTrue(recipient.getId());

    if (activeTokens.isEmpty()) return;

    List<String> tokenStrings =
            activeTokens.stream()
                    .map(DeviceToken::getToken)
                    .toList();

    try {

      MulticastMessage message = MulticastMessage.builder()
              .addAllTokens(tokenStrings)
              .putData("sessionId", session.getId().toString())
              .putData("content", content)
              .build();

      BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

      // Handle invalid tokens
      for (int i = 0; i < response.getResponses().size(); i++) {

        SendResponse sendResponse = response.getResponses().get(i);

        if (!sendResponse.isSuccessful()) {

          FirebaseMessagingException ex = sendResponse.getException();

          if (ex.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {

            String badToken = tokenStrings.get(i);

            deviceTokenRepository.findByToken(badToken)
                    .ifPresent(token -> {
                      token.setIsActive(false);
                      deviceTokenRepository.save(token);
                      log.info("Disabled invalid FCM token: {}", badToken);
                    });
          }
        }
      }

    } catch (Exception e) {
      log.error("Push notification failed for session {}", session.getId(), e);
    }
  }

  private void syncSessionToFirestore(ChatSession session, boolean isNew) {
    Map<String, Object> doc = new HashMap<>();
    doc.put("customerId", session.getCustomer().getId());
    doc.put("customerName", session.getCustomer().getFullName());
    doc.put("customerAvatar", session.getCustomer().getAvatar());
    doc.put("agentId", session.getAgent() != null ? session.getAgent().getId() : null);
    doc.put("agentName", session.getAgent() != null ? session.getAgent().getFullName() : null);
    doc.put("status", session.getStatus().name());
    doc.put("unreadCount", 0);
    doc.put("updatedAt", Timestamp.now());

    if (isNew) {
      doc.put("createdAt", Timestamp.now());
      doc.put("lastMessage", null);
      doc.put("lastMessageAt", null);
      firestore.collection(SESSIONS_COL).document(session.getId().toString()).set(doc);
    } else {
      // merge: preserves createdAt and lastMessage fields
      firestore
          .collection(SESSIONS_COL)
          .document(session.getId().toString())
          .set(doc, SetOptions.merge());
    }
  }

  private long countUnread(Long sessionId) {
    return messageRepository.countBySessionIdAndIsReadFalseAndSenderTypeNot(
        sessionId, SenderType.AGENT);
  }

  private ChatSession getSessionOrThrow(Long sessionId) {
    return sessionRepository
        .findById(sessionId)
        .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
  }
}
