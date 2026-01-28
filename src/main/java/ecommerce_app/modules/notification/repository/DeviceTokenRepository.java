package ecommerce_app.modules.notification.repository;

import ecommerce_app.modules.notification.model.entity.DeviceToken;
import ecommerce_app.modules.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

  // Find by token
  Optional<DeviceToken> findByToken(String token);

  // Find active tokens by user
  List<DeviceToken> findByUserAndIsActiveTrue(User user);

  // Find all tokens by user
  List<DeviceToken> findByUser(User user);

  // Delete by token
  @Modifying
  @Query("DELETE FROM DeviceToken d WHERE d.token = :token")
  void deleteByToken(@Param("token") String token);

  // Deactivate old tokens
  @Modifying
  @Query(
      "UPDATE DeviceToken d SET d.isActive = false WHERE d.lastUsedAt < :cutoffDate AND d.isActive = true")
  int deactivateOldTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

  // Count active tokens by user
  Long countByUserAndIsActiveTrue(User user);
}
