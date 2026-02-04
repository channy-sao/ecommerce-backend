package ecommerce_app.infrastructure.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class FullAuditableEntity extends UserAuditableEntity {

  @Column(name = "deleted", nullable = false)
  private boolean deleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  private Long deletedBy;

  public void softDelete(Long deletedBy) {
    this.deleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
  }
}
