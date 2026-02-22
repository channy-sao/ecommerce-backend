package ecommerce_app.repository;

import ecommerce_app.constant.enums.PermissionEnum;
import ecommerce_app.entity.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
  Optional<Permission> findByName(PermissionEnum name);
}
