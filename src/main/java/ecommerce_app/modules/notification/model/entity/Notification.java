package ecommerce_app.modules.notification.model.entity;

import ecommerce_app.constant.enums.NotificationStatus;
import ecommerce_app.constant.enums.NotificationType;
import ecommerce_app.infrastructure.model.entity.AuditingEntity;
import ecommerce_app.modules.user.model.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "notifications",
    indexes = {
      @Index(name = "idx_status", columnList = "status"),
      @Index(name = "idx_type", columnList = "type"),
      @Index(name = "idx_created_at", columnList = "created_at"),
      @Index(name = "idx_is_read", columnList = "is_read"),
      @Index(name = "idx_user_status", columnList = "user_id, status"),
      @Index(name = "idx_user_read", columnList = "user_id, is_read"),
      @Index(name = "idx_expires_at", columnList = "expires_at")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** User who receives this notification */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "user_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_notification_user"))
  private User user;

  /** Notification title (required) */
  @Column(name = "title", nullable = false, length = 255)
  private String title;

  /** Notification message body (required) */
  @Column(name = "message", nullable = false, length = 1000, columnDefinition = "TEXT")
  private String message;

  /** Type of notification (ORDER, PAYMENT, SHIPPING, etc.) */
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 50)
  private NotificationType type;

  /** Current status (PENDING, SENT, DELIVERED, FAILED, READ) */
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  @Builder.Default
  private NotificationStatus status = NotificationStatus.PENDING;

  /** Reference ID to related entity (e.g., order ID, payment ID) */
  @Column(name = "reference_id", length = 100)
  private String referenceId;

  /** Type of referenced entity (e.g., "ORDER", "PAYMENT") */
  @Column(name = "reference_type", length = 50)
  private String referenceType;

  /** URL to navigate when notification is clicked */
  @Column(name = "action_url", length = 500)
  private String actionUrl;

  /** Image URL for rich notifications */
  @Column(name = "image_url", length = 500)
  private String imageUrl;

  /** Additional data in JSON format */
  @Column(name = "data", columnDefinition = "TEXT")
  private String data;

  /** Whether notification has been read */
  @Column(name = "is_read", nullable = false)
  @Builder.Default
  private Boolean isRead = false;

  /** Timestamp when notification was read */
  @Column(name = "read_at")
  private LocalDateTime readAt;

  /** Whether notification has been sent via FCM */
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Column(name = "is_sent", nullable = false)
  @Builder.Default
  private Boolean isSent = false;

  /** Timestamp when notification was sent via FCM */
  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  /** Timestamp when notification was delivered to device */
  @Column(name = "delivered_at")
  private LocalDateTime deliveredAt;

  /** Error message if notification failed */
  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  /** Number of retry attempts */
  @Column(name = "retry_count", nullable = false)
  @Builder.Default
  private Integer retryCount = 0;

  /** Maximum number of retry attempts */
  @Column(name = "max_retry_count", nullable = false)
  @Builder.Default
  private Integer maxRetryCount = 3;

  /** Priority level (1-10, higher = more important) */
  @Column(name = "priority")
  @Builder.Default
  private Integer priority = 5;

  /** Expiration timestamp (auto-delete after this) */
  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  /** Category for grouping notifications */
  @Column(name = "category", length = 50)
  private String category;

  /** Tags for filtering (comma-separated) */
  @Column(name = "tags", length = 255)
  private String tags;

  /** Sound to play (default, silent, custom) */
  @Column(name = "sound", length = 50)
  @Builder.Default
  private String sound = "default";

  /** Badge count for app icon */
  @Column(name = "badge")
  private Integer badge;

  /** Thread ID for grouping related notifications */
  @Column(name = "thread_id", length = 100)
  private String threadId;

  /** Whether notification is silent (no alert) */
  @Column(name = "is_silent")
  @Builder.Default
  private Boolean isSilent = false;

  /** Whether notification should be saved to database */
  @Transient private Boolean saveToDatabase = true;

  /** Whether to send push notification */
  @Transient private Boolean sendPush = true;

  /** Soft delete flag */
  @Column(name = "is_deleted")
  @Builder.Default
  private Boolean isDeleted = false;

  /** Timestamp when record was deleted */
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  // ==================== Helper Methods ====================

  /** Mark notification as read */
  public void markAsRead() {
    this.isRead = true;
    this.readAt = LocalDateTime.now();
    if (this.status == NotificationStatus.SENT || this.status == NotificationStatus.DELIVERED) {
      this.status = NotificationStatus.READ;
    }
  }

  /** Mark notification as sent */
  public void markAsSent() {
    this.status = NotificationStatus.SENT;
    this.isSent = true;
    this.sentAt = LocalDateTime.now();
  }

  /** Mark notification as delivered */
  public void markAsDelivered() {
    this.status = NotificationStatus.DELIVERED;
    this.deliveredAt = LocalDateTime.now();
  }

  /** Mark notification as failed */
  public void markAsFailed(String errorMessage) {
    this.status = NotificationStatus.FAILED;
    this.errorMessage = errorMessage;
    this.retryCount++;
  }

  /** Check if notification can be retried */
  public boolean canRetry() {
    return this.status == NotificationStatus.FAILED && this.retryCount < this.maxRetryCount;
  }

  /** Check if notification is expired */
  public boolean isExpired() {
    return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
  }

  /** Soft delete notification */
  public void softDelete() {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
  }

  /** Check if notification is actionable (has action URL) */
  public boolean isActionable() {
    return this.actionUrl != null && !this.actionUrl.trim().isEmpty();
  }

  /** Get user ID (convenience method) */
  public Long getUserId() {
    return user != null ? user.getId() : null;
  }

  // ==================== Lifecycle Callbacks ====================

  /** Pre-persist hook */
  @PrePersist
  protected void onCreate() {
    if (status == null) {
      status = NotificationStatus.PENDING;
    }
    if (isRead == null) {
      isRead = false;
    }
    if (isSent == null) {
      isSent = false;
    }
    if (retryCount == null) {
      retryCount = 0;
    }
    if (maxRetryCount == null) {
      maxRetryCount = 3;
    }
    if (priority == null) {
      priority = 5;
    }
    if (sound == null) {
      sound = "default";
    }
    if (isSilent == null) {
      isSilent = false;
    }
    if (isDeleted == null) {
      isDeleted = false;
    }
  }

  /** Pre-update hook */
  @PreUpdate
  protected void onUpdate() {
    // Automatically set readAt when isRead changes to true45
    if (Boolean.TRUE.equals(this.isRead) && this.readAt == null) {
      this.readAt = LocalDateTime.now();
    }
    if (Boolean.TRUE.equals(this.isSent) && this.sentAt == null) {
      this.sentAt = LocalDateTime.now();
    }
  }
}
