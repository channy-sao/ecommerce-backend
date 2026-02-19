package ecommerce_app.modules.setting.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

  @Id
  @Column(name = "setting_key", length = 100)
  private String key;

  @Column(nullable = false, columnDefinition = "TEXT", name = "value")
  private String value;

  @Column(length = 200, name = "label")
  private String label;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  void touch() {
    this.updatedAt = LocalDateTime.now();
  }
}
