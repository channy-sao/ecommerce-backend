package ecommerce_app.infrastructure.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
@Setter
public abstract class BaseAuditingEntity {
  @Column(name = "created_at", nullable = false, updatable = false)
//  @Temporal(TemporalType.TIMESTAMP)  // Not be use with Instant
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
//  @Temporal(TemporalType.TIMESTAMP)  // Not be use with Instant
  @LastModifiedDate
  private LocalDateTime updatedAt;
}
