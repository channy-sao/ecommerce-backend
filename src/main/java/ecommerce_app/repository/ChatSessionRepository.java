// ChatSessionRepository.java
package ecommerce_app.repository;

import ecommerce_app.constant.enums.SessionStatus;
import ecommerce_app.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

  Optional<ChatSession> findByCustomerIdAndStatus(Long customerId, SessionStatus status);

  boolean existsByCustomerIdAndStatusIn(Long customerId, List<SessionStatus> statuses);

  List<ChatSession> findByStatusOrderByCreatedAtAsc(SessionStatus status);

  List<ChatSession> findByAgentIdOrderByCreatedAtDesc(Long agentId);

  List<ChatSession> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}