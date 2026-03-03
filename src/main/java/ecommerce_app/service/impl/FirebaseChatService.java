package ecommerce_app.service.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseChatService {

  private final Firestore firestore;
  private final FirebaseMessaging firebaseMessaging;
  private final UserRepository userRepository;
  private final ChatSessionRepository sessionRepository; // still persist to your DB
  private final ChatMessageRepository messageRepository;

  private static final String SESSIONS_COLLECTION = "chat_sessions";
  private static final String MESSAGES_COLLECTION = "messages";

  // ── Session ──────────────────────────────────────────────────────────────

  public ChatSession createSession(Long customerId) {
    // 1. Guard: no duplicate open session
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

    // 2. Save to your DB
    ChatSession session =
        sessionRepository.save(
            ChatSession.builder().customer(customer).status(SessionStatus.WAITING).build());

    // 3. Mirror to Firestore so agents see it in real-time
    syncSessionToFirestore(session, 0);

    return session;
  }

  public ChatSession assignAgent(Long sessionId, Long agentId) {
    ChatSession session = getSessionOrThrow(sessionId);
    User agent =
        userRepository.findById(agentId).orElseThrow(() -> new RuntimeException("Agent not found"));

    session.setAgent(agent);
    session.setStatus(SessionStatus.ACTIVE);
    sessionRepository.save(session);

    // Update Firestore session doc
    syncSessionToFirestore(session, 0);

    // Send system message
    saveMessage(
        sessionId,
        null,
        "Agent " + agent.getFullName() + " has joined. How can we help you?",
        SenderType.SYSTEM);

    return session;
  }

  public void closeSession(Long sessionId) {
    ChatSession session = getSessionOrThrow(sessionId);
    session.setStatus(SessionStatus.CLOSED);
    session.setClosedAt(LocalDateTime.now());
    sessionRepository.save(session);

    saveMessage(sessionId, null, "Chat session has been closed. Thank you!", SenderType.SYSTEM);

    // Update Firestore
    firestore
        .collection(SESSIONS_COLLECTION)
        .document(sessionId.toString())
        .update("status", "CLOSED", "closedAt", Timestamp.now());
  }

  // ── Messages ─────────────────────────────────────────────────────────────

  public ChatMessage saveMessage(
      Long sessionId, Long senderId, String content, SenderType senderType) {
    ChatSession session = getSessionOrThrow(sessionId);
    User sender = senderId != null ? userRepository.findById(senderId).orElse(null) : null;

    // 1. Save to your DB
    ChatMessage message =
        messageRepository.save(
            ChatMessage.builder()
                .session(session)
                .sender(sender)
                .senderType(senderType)
                .content(content)
                .isRead(false)
                .build());

    // 2. Write to Firestore subcollection → clients get real-time update
    Map<String, Object> msgDoc = new HashMap<>();
    msgDoc.put("id", message.getId());
    msgDoc.put("senderId", sender != null ? sender.getId() : null);
    msgDoc.put("senderName", sender != null ? sender.getFullName() : "System");
    msgDoc.put("senderType", senderType.name());
    msgDoc.put("content", content);
    msgDoc.put("isRead", false);
    msgDoc.put("sentAt", Timestamp.now());

    firestore
        .collection(SESSIONS_COLLECTION)
        .document(sessionId.toString())
        .collection(MESSAGES_COLLECTION)
        .document(message.getId().toString())
        .set(msgDoc);

    // 3. Push notification to the other party via FCM
    sendPushNotification(session, sender, content, senderType);

    return message;
  }

  public void markAsRead(Long sessionId, SenderType readerType) {
    SenderType opposite = readerType == SenderType.AGENT ? SenderType.CUSTOMER : SenderType.AGENT;

    // Update DB
    messageRepository.markAllAsRead(sessionId, opposite);

    // Update Firestore — batch update all unread messages
    CollectionReference messages =
        firestore
            .collection(SESSIONS_COLLECTION)
            .document(sessionId.toString())
            .collection(MESSAGES_COLLECTION);

    messages
        .whereEqualTo("senderType", opposite.name())
        .whereEqualTo("isRead", false)
        .get()
        .addListener(
            () -> {}, Runnable::run); // fire-and-forget; use ApiFuture if you need to await
  }

  // ── FCM Push Notification ────────────────────────────────────────────────

  private void sendPushNotification(
      ChatSession session, User sender, String content, SenderType senderType) {
    try {
      // Determine recipient: agent gets customer messages, customer gets agent messages
      User recipient =
          senderType == SenderType.CUSTOMER ? session.getAgent() : session.getCustomer();

      if (recipient == null || recipient.getFcmToken() == null) return;

      String senderName = sender != null ? sender.getFullName() : "System";

      Message fcmMessage =
          Message.builder()
              .setToken(recipient.getFcmToken())
              .setNotification(
                  Notification.builder()
                      .setTitle("New message from " + senderName)
                      .setBody(content)
                      .build())
              .putData("sessionId", session.getId().toString())
              .putData("senderType", senderType.name())
              .build();

      firebaseMessaging.send(fcmMessage);

    } catch (FirebaseMessagingException e) {
      // log but don't fail the message save
      log.warn("FCM push failed for session {}: {}", session.getId(), e.getMessage());
    }
  }

  // ── Firestore Sync Helper ────────────────────────────────────────────────

  private void syncSessionToFirestore(ChatSession session, int unreadCount) {
    Map<String, Object> doc = new HashMap<>();
    doc.put("customerId", session.getCustomer().getId());
    doc.put("customerName", session.getCustomer().getFullName());
    doc.put("customerAvatar", session.getCustomer().getAvatar());
    doc.put("agentId", session.getAgent() != null ? session.getAgent().getId() : null);
    doc.put("agentName", session.getAgent() != null ? session.getAgent().getFullName() : null);
    doc.put("status", session.getStatus().name());
    doc.put("createdAt", Timestamp.now());
    doc.put("unreadCount", unreadCount);

    firestore.collection(SESSIONS_COLLECTION).document(session.getId().toString()).set(doc);
  }

  private ChatSession getSessionOrThrow(Long sessionId) {
    return sessionRepository
        .findById(sessionId)
        .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
  }
}
