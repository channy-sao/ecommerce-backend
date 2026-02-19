package ecommerce_app.modules.setting.repository;

import ecommerce_app.modules.setting.model.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {
}