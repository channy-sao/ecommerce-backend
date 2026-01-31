package ecommerce_app.modules.user.repository;

import ecommerce_app.constant.enums.AuthProvider;
import ecommerce_app.modules.user.model.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndAuthProviderNot(String email, AuthProvider authProvider);

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :userId")
  Optional<User> findByIdWithRoles(@Param("userId") Long userId);

  @Query(
"""
    SELECT u FROM User u
    LEFT JOIN FETCH u.roles r
    LEFT JOIN FETCH r.permissions
    WHERE u.email = :email
""")
  Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

  Optional<User> findByPhone(String phone);

  boolean existsByEmailAndIdNot(String email, Long id);

  boolean existsByPhoneAndIdNot(String phone, Long id);

  // Direct update query (avoids entity state issues)
  @Modifying
  @Query("UPDATE User u SET u.isActive = :status WHERE u.id = :userId")
  int updateUserStatus(@Param("userId") Long userId, @Param("status") Boolean status);

  // Check if user is super admin without loading full entity
  @Query(
      "SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END "
          + "FROM User u JOIN u.roles r "
          + "WHERE u.id = :userId AND r.name = :roleName")
  boolean hasRole(@Param("userId") Long userId, @Param("roleName") String roleName);
}
