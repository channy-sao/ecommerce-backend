package ecommerce_app.infrastructure.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity extends AuditingEntity {
  @Column(name = "is_deleted")
  private boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @LastModifiedBy
  @Column(name = "deleted_by")
  private Long deletedBy;

  public void softDelete() {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
  }
}
