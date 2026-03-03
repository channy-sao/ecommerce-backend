// ChatMessageRepository.java
package ecommerce_app.repository;

import ecommerce_app.constant.enums.SenderType;
import ecommerce_app.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

  long countBySessionIdAndIsReadFalseAndSenderTypeNot(Long sessionId, SenderType senderType);

  @Modifying
  @Query("""
        UPDATE ChatMessage m SET m.isRead = true
        WHERE m.session.id = :sessionId
          AND m.senderType = :senderType
          AND m.isRead = false
        """)
  void markAllAsRead(@Param("sessionId") Long sessionId,
                     @Param("senderType") SenderType senderType);
}