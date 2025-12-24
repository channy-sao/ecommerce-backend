package ecommerce_app.modules.user.repository;

import ecommerce_app.modules.user.model.entity.Role;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);

  Set<Role> findByNameContaining(String name);
}
