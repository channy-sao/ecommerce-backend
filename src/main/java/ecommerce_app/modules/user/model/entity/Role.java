package ecommerce_app.modules.user.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.infrastructure.model.entity.BaseSoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "roles")
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseSoftDeleteEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "uid", length = 75, unique = true)
  private String uid;

  @Column(name = "role_name", length = 100, unique = true, nullable = false)
  private String name;

  @Column(name = "description", length = 250)
  private String description;

  @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
  @JsonIgnore
  private Set<User> users;

  // Role owns permissions
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions = new HashSet<>();
}
