package ecommerce_app.infrastructure.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseSoftDeleteEntity {
  @Column(name = "is_deleted")
  private Boolean isDeleted = false;

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
