package ecommerce_app.repository;

import ecommerce_app.constant.enums.SessionStatus;
import ecommerce_app.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
  List<ChatSession> findByStatus(SessionStatus status);

  List<ChatSession> findByCustomerId(Long customerId);

  Optional<ChatSession> findByCustomerIdAndStatus(Long customerId, SessionStatus status);
}
