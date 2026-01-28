package ecommerce_app.modules.notification.repository;

import ecommerce_app.modules.notification.model.entity.Notification;
import ecommerce_app.modules.notification.model.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

  // Find logs by notification
  List<NotificationLog> findByNotification(Notification notification);

  // Find failed logs
  List<NotificationLog> findByStatus(String status);

  // Count by status
  @Query("SELECT COUNT(nl) FROM NotificationLog nl WHERE nl.status = :status")
  Long countByStatus(@Param("status") String status);

  // Find logs in time range
  List<NotificationLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
