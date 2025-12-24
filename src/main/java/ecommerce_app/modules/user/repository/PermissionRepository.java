package ecommerce_app.modules.user.repository;

import ecommerce_app.constant.enums.PermissionEnum;
import ecommerce_app.modules.user.model.entity.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
  Optional<Permission> findByName(PermissionEnum name);
}
