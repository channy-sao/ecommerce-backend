package ecommerce_app.modules.notification.repository;

import ecommerce_app.constant.enums.NotificationStatus;
import ecommerce_app.constant.enums.NotificationType;
import ecommerce_app.modules.notification.model.entity.Notification;
import ecommerce_app.modules.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Notification Repository Provides database operations for Notification entity
 *
 * @author Your Name
 * @version 1.0
 */
@Repository
public interface NotificationRepository
    extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

  // ==================== Basic Queries ====================

  /** Find all notifications for a user (paginated, newest first) */
  Page<Notification> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

  /** Find all notifications for a user (non-paginated) */
  List<Notification> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user);

  /** Find notification by ID and user (security check) */
  Optional<Notification> findByIdAndUserAndIsDeletedFalse(Long id, User user);

  // ==================== Unread Notifications ====================

  /** Find unread notifications for a user */
  Page<Notification> findByUserAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtDesc(
      User user, Pageable pageable);

  /** Count unread notifications for a user */
  @Query(
      "SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false AND n.isDeleted = false")
  Long countUnreadByUser(@Param("user") User user);

  /** Get unread count by user ID (more efficient) */
  @Query(
      "SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false AND n.isDeleted = false")
  Long countUnreadByUserId(@Param("userId") Long userId);

  // ==================== Filter by Type ====================

  /** Find notifications by user and type */
  Page<Notification> findByUserAndTypeAndIsDeletedFalseOrderByCreatedAtDesc(
      User user, NotificationType type, Pageable pageable);

  /** Find unread notifications by user and type */
  Page<Notification> findByUserAndTypeAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtDesc(
      User user, NotificationType type, Pageable pageable);

  /** Count notifications by type for a user */
  @Query(
      "SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.type = :type AND n.isDeleted = false")
  Long countByUserAndType(@Param("user") User user, @Param("type") NotificationType type);

  // ==================== Filter by Status ====================

  /** Find notifications by status */
  List<Notification> findByStatusAndIsDeletedFalse(NotificationStatus status);

  /** Find notifications by user and status */
  Page<Notification> findByUserAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
      User user, NotificationStatus status, Pageable pageable);

  /** Count notifications by status */
  @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status AND n.isDeleted = false")
  Long countByStatus(@Param("status") NotificationStatus status);

  // ==================== Retry Logic ====================

  /** Find failed notifications that can be retried */
  @Query(
      "SELECT n FROM Notification n WHERE n.status = :status "
          + "AND n.retryCount < n.maxRetryCount "
          + "AND n.isDeleted = false "
          + "ORDER BY n.createdAt ASC")
  List<Notification> findPendingForRetry(@Param("status") NotificationStatus status);

  /** Find failed notifications with retry limit */
  @Query(
      "SELECT n FROM Notification n WHERE n.status = :status "
          + "AND n.retryCount < :maxRetries "
          + "AND n.isDeleted = false "
          + "ORDER BY n.createdAt ASC")
  List<Notification> findPendingForRetry(
      @Param("status") NotificationStatus status, @Param("maxRetries") Integer maxRetries);

  /** Find notifications for retry within time window */
  @Query(
      "SELECT n FROM Notification n WHERE n.status = :status "
          + "AND n.retryCount < n.maxRetryCount "
          + "AND n.createdAt BETWEEN :startDate AND :endDate "
          + "AND n.isDeleted = false "
          + "ORDER BY n.priority DESC, n.createdAt ASC")
  List<Notification> findPendingForRetryInTimeWindow(
      @Param("status") NotificationStatus status,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  // ==================== Mark as Read Operations ====================

  /** Mark single notification as read */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, n.status = :status "
          + "WHERE n.id = :id AND n.isDeleted = false")
  int markAsRead(
      @Param("id") Long id,
      @Param("readAt") LocalDateTime readAt,
      @Param("status") NotificationStatus status);

  /** Mark all notifications as read for a user */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, n.status = :status "
          + "WHERE n.user = :user AND n.isRead = false AND n.isDeleted = false")
  int markAllAsRead(
      @Param("user") User user,
      @Param("readAt") LocalDateTime readAt,
      @Param("status") NotificationStatus status);

  /** Mark notifications of specific type as read */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true, n.readAt = :readAt "
          + "WHERE n.user = :user AND n.type = :type AND n.isRead = false AND n.isDeleted = false")
  int markTypeAsRead(
      @Param("user") User user,
      @Param("type") NotificationType type,
      @Param("readAt") LocalDateTime readAt);

  /** Mark specific notifications as read (bulk operation) */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true, n.readAt = :readAt "
          + "WHERE n.id IN :ids AND n.isDeleted = false")
  int markMultipleAsRead(@Param("ids") List<Long> ids, @Param("readAt") LocalDateTime readAt);

  // ==================== Reference Queries ====================

  /** Find notifications by reference ID and type */
  List<Notification> findByReferenceIdAndReferenceTypeAndIsDeletedFalse(
      String referenceId, String referenceType);

  /** Find notifications by reference ID */
  List<Notification> findByReferenceIdAndIsDeletedFalse(String referenceId);

  /** Check if notification exists for reference */
  boolean existsByReferenceIdAndReferenceTypeAndUserAndIsDeletedFalse(
      String referenceId, String referenceType, User user);

  // ==================== Priority & Category ====================

  /** Find high priority notifications */
  @Query(
      "SELECT n FROM Notification n WHERE n.user = :user "
          + "AND n.priority >= :minPriority "
          + "AND n.isDeleted = false "
          + "ORDER BY n.priority DESC, n.createdAt DESC")
  Page<Notification> findHighPriorityNotifications(
      @Param("user") User user, @Param("minPriority") Integer minPriority, Pageable pageable);

  /** Find notifications by category */
  Page<Notification> findByUserAndCategoryAndIsDeletedFalseOrderByCreatedAtDesc(
      User user, String category, Pageable pageable);

  // ==================== Thread & Grouping ====================

  /** Find notifications in a thread */
  List<Notification> findByThreadIdAndIsDeletedFalseOrderByCreatedAtDesc(String threadId);

  /** Find notifications by thread and user */
  List<Notification> findByUserAndThreadIdAndIsDeletedFalseOrderByCreatedAtDesc(
      User user, String threadId);

  // ==================== Time-based Queries ====================

  /** Find notifications created after a certain date */
  Page<Notification> findByUserAndCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtDesc(
      User user, LocalDateTime afterDate, Pageable pageable);

  /** Find notifications within date range */
  @Query(
      "SELECT n FROM Notification n WHERE n.user = :user "
          + "AND n.createdAt BETWEEN :startDate AND :endDate "
          + "AND n.isDeleted = false "
          + "ORDER BY n.createdAt DESC")
  Page<Notification> findByUserAndDateRange(
      @Param("user") User user,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);



  /** Hard delete expired notifications */
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
  int deleteExpiredNotifications(@Param("now") LocalDateTime now);

  // ==================== Cleanup Operations ====================

  /** Soft delete old read notifications */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isDeleted = true, n.deletedAt = :now "
          + "WHERE n.isRead = true "
          + "AND n.createdAt < :cutoffDate "
          + "AND n.isDeleted = false")
  int softDeleteOldReadNotifications(
      @Param("cutoffDate") LocalDateTime cutoffDate, @Param("now") LocalDateTime now);

  /** Hard delete old read notifications */
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :cutoffDate")
  int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

  /** Soft delete all notifications for a user */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isDeleted = true, n.deletedAt = :now "
          + "WHERE n.user = :user AND n.isDeleted = false")
  int softDeleteAllForUser(@Param("user") User user, @Param("now") LocalDateTime now);

  // ==================== Statistics ====================

  /** Get notification statistics for a user */
  @Query(
      "SELECT "
          + "COUNT(n) as total, "
          + "SUM(CASE WHEN n.isRead = false THEN 1 ELSE 0 END) as unread, "
          + "SUM(CASE WHEN n.status = 'SENT' THEN 1 ELSE 0 END) as sent, "
          + "SUM(CASE WHEN n.status = 'FAILED' THEN 1 ELSE 0 END) as failed "
          + "FROM Notification n WHERE n.user = :user AND n.isDeleted = false")
  Object[] getNotificationStats(@Param("user") User user);

  /** Count notifications by status for a user */
  @Query(
      "SELECT n.status, COUNT(n) FROM Notification n "
          + "WHERE n.user = :user AND n.isDeleted = false "
          + "GROUP BY n.status")
  List<Object[]> countByStatusForUser(@Param("user") User user);

  /** Count notifications by type for a user */
  @Query(
      "SELECT n.type, COUNT(n) FROM Notification n "
          + "WHERE n.user = :user AND n.isDeleted = false "
          + "GROUP BY n.type")
  List<Object[]> countByTypeForUser(@Param("user") User user);

  // ==================== Search ====================

  /** Search notifications by title or message */
  @Query(
      "SELECT n FROM Notification n WHERE n.user = :user "
          + "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
          + "OR LOWER(n.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) "
          + "AND n.isDeleted = false "
          + "ORDER BY n.createdAt DESC")
  Page<Notification> searchNotifications(
      @Param("user") User user, @Param("searchTerm") String searchTerm, Pageable pageable);

  /** Search unread notifications */
  @Query(
      "SELECT n FROM Notification n WHERE n.user = :user "
          + "AND n.isRead = false "
          + "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
          + "OR LOWER(n.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) "
          + "AND n.isDeleted = false "
          + "ORDER BY n.createdAt DESC")
  Page<Notification> searchUnreadNotifications(
      @Param("user") User user, @Param("searchTerm") String searchTerm, Pageable pageable);

  // ==================== Advanced Queries ====================

  /** Find actionable unread notifications */
  @Query(
      "SELECT n FROM Notification n WHERE n.user = :user "
          + "AND n.isRead = false "
          + "AND n.actionUrl IS NOT NULL "
          + "AND n.isDeleted = false "
          + "ORDER BY n.priority DESC, n.createdAt DESC")
  Page<Notification> findActionableUnreadNotifications(@Param("user") User user, Pageable pageable);

  /** Find recent high priority unread notifications */
  @Query(
      "SELECT n FROM Notification n WHERE n.user = :user "
          + "AND n.isRead = false "
          + "AND n.priority >= :minPriority "
          + "AND n.createdAt >= :since "
          + "AND n.isDeleted = false "
          + "ORDER BY n.priority DESC, n.createdAt DESC")
  List<Notification> findRecentHighPriorityUnread(
      @Param("user") User user,
      @Param("minPriority") Integer minPriority,
      @Param("since") LocalDateTime since);

  /** Get latest notification for each type */
  @Query(
      "SELECT n FROM Notification n WHERE n.user = :user "
          + "AND n.id IN ("
          + "  SELECT MAX(n2.id) FROM Notification n2 "
          + "  WHERE n2.user = :user AND n2.isDeleted = false "
          + "  GROUP BY n2.type"
          + ") AND n.isDeleted = false")
  List<Notification> findLatestByType(@Param("user") User user);

  // ==================== Batch Operations ====================

  /** Update status for multiple notifications */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.status = :status "
          + "WHERE n.id IN :ids AND n.isDeleted = false")
  int updateStatusForMultiple(
      @Param("ids") List<Long> ids, @Param("status") NotificationStatus status);

  /** Soft delete multiple notifications */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isDeleted = true, n.deletedAt = :now "
          + "WHERE n.id IN :ids AND n.isDeleted = false")
  int softDeleteMultiple(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);
}
