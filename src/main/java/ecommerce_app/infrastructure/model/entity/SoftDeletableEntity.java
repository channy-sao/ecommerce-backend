package ecommerce_app.infrastructure.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class SoftDeletableEntity
        extends UserAuditableEntity {

  @Column(name = "deleted", nullable = false)
  private boolean deleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  private Long deletedBy;

  public void softDelete(Long userId) {
    this.deleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = userId;
  }

  public void restore() {
    this.deleted = false;
    this.deletedAt = null;
    this.deletedBy = null;
  }
}

