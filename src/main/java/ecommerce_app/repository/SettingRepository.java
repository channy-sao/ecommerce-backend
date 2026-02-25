package ecommerce_app.repository;

import ecommerce_app.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {
  @Query("SELECT s FROM Setting s WHERE s.key IN :keys")
  List<Setting> findByKeys(@Param("keys") List<String> keys);
}
