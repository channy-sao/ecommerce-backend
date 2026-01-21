package ecommerce_app.modules.user.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.infrastructure.model.entity.BaseAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(
    name = "users",
    indexes = {
      @Index(columnList = "firstName", name = "first_name_index"),
      @Index(columnList = "lastName", name = "last_name_index"),
      @Index(columnList = "uuid", name = "uuid_index"),
    })
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "uid", length = 75, nullable = true)
  private String uid;

  @Column(name = "password", length = 250)
  private String password;

  @Column(nullable = false, name = "email", unique = true, length = 100)
  private String email;

  @Column(name = "phone", nullable = true, length = 25)
  private String phone;

  @Column(name = "first_name", length = 50, nullable = true)
  private String firstName;

  @Column(length = 50, name = "last_name", nullable = true)
  private String lastName;

  @Column(name = "avatar", length = 512)
  private String avatar;

  @Column(name = "provider")
  private String provider;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "email_verified_at")
  private LocalDateTime emailVerifiedAt;

  @Column(name = "email_verified")
  private Boolean isEmailVerified;

  @Column(name = "remember_me", nullable = false)
  private Boolean rememberMe = false;

  @Column(name = "uuid", nullable = false, unique = true, updatable = false)
  private UUID uuid;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @JsonIgnore
  private Set<Role> roles = new HashSet<>();

  public String getFullName() {
    return firstName + " " + lastName;
  }

  // pre persist
  @PrePersist
  public void prePersist() {
    if (uuid == null) {
      uuid = UUID.randomUUID();
    }
  }
}
