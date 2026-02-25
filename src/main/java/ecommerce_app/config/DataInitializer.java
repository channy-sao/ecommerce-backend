package ecommerce_app.config;

import ecommerce_app.constant.enums.AuthProvider;
import ecommerce_app.constant.enums.PermissionEnum;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.entity.Setting;
import ecommerce_app.repository.SettingRepository;
import ecommerce_app.entity.Permission;
import ecommerce_app.entity.Role;
import ecommerce_app.entity.User;
import ecommerce_app.repository.PermissionRepository;
import ecommerce_app.repository.RoleRepository;
import ecommerce_app.repository.UserRepository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ecommerce_app.util.ImageDownloadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(value = 1)
public class DataInitializer implements ApplicationRunner {
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final SettingRepository settingRepository;
  public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final StorageConfig storageConfig;

  @Transactional
  @Override
  public void run(ApplicationArguments args) throws Exception {
    initSetting();
    seedPermissions();
    seedSuperAdminRole();
    initSuperAdmin();
  }

  private void seedPermissions() {

    try {
      for (var permissionEnum : PermissionEnum.values()) {
        permissionRepository
            .findByName(permissionEnum)
            .orElseGet(
                () ->
                    permissionRepository.save(
                        Permission.builder()
                            .name(permissionEnum)
                            .category(permissionEnum.getCategory())
                            .description(permissionEnum.getDescription())
                            .build()));
        log.info("Permission {} has been created", permissionEnum.name());
      }
    } catch (Exception _) {
      log.info("Permissions not created");
    }
  }

  private void seedSuperAdminRole() {
    try {
      roleRepository
          .findByName(SUPER_ADMIN_ROLE)
          .ifPresentOrElse(
              d -> log.info("Role {} has been created", SUPER_ADMIN_ROLE),
              () ->
                  roleRepository.save(
                      Role.builder()
                          .name(SUPER_ADMIN_ROLE)
                          .permissions(new HashSet<>(permissionRepository.findAll()))
                          .isActive(true)
                          .uid(UUID.randomUUID().toString())
                          .build()));
    } catch (Exception _) {
      log.info("Roles not created");
    }
  }

  private void initSuperAdmin() {

    // Check if super admin already exists
    userRepository
        .findByEmail("admin@gmail.com")
        .ifPresentOrElse(
            user -> {
              log.info("Admin already exists");
              return;
            },
            () -> {
              Role role =
                  roleRepository
                      .findByName(DataInitializer.SUPER_ADMIN_ROLE)
                      .orElseThrow(
                          () -> new ResourceNotFoundException("Super admin role is not found"));
              // Create super admin user
              User admin =
                  User.builder()
                      .email("admin@gmail.com")
                      .password(passwordEncoder.encode("admin@123"))
                      .avatar("admin-avatar.png")
                      .firstName("Admin")
                      .lastName("Admin")
                      .phone("+855 356789")
                      .uuid(UUID.randomUUID())
                      .isActive(true)
                      .authProvider(AuthProvider.LOCAL)
                      .rememberMe(true)
                      .emailVerifiedAt(LocalDateTime.now())
                      .isEmailVerified(false)
                      .roles(Set.of(role))
                      .build();

              try {
                String imagePath =
                    ImageDownloadUtils.downloadAndSave(
                        "https://picsum.photos/300/300", storageConfig.getAvatarPath());

                admin.setAvatar(Path.of(imagePath).getFileName().toString());

              } catch (Exception e) {
                log.error("Failed to download image for user : {}", e.getMessage());
                admin.setAvatar(null);
              }

              userRepository.save(admin);

              log.info("Super Admin initialized successfully!");
            });
  }

  public void initSetting() {
    List<Setting> defaults = defaultSettings();

    int inserted = 0;
    for (Setting setting : defaults) {
      // Only insert if key does NOT already exist — never overwrite admin changes
      if (!settingRepository.existsById(setting.getKey())) {
        settingRepository.save(setting);
        inserted++;
      }
    }

    if (inserted > 0) {
      log.info("[Settings] Inserted {} new default setting(s)", inserted);
    } else {
      log.info("[Settings] All settings already exist — nothing to insert");
    }
  }

  private List<Setting> defaultSettings() {
    return List.of(
        // ── Store ──────────────────────────────────────────────────────────
        build("store.name", "My Shop", "Store Name"),
        build("store.email", "mystore@gmail.com.kh", "Contact Email"),
        build("store.phone", "+855 12334477", "Contact Phone"),
        build("store.address", "Street 271, Stueng Meanchey, Phnom Penh, Cambodia", "Store Address"),
        build("store.currency", "USD", "Currency"),
        build("store.logo_url", "", "Logo URL"),
        build("store.facebook_url", "", "Facebook URL"),
        build("store.open_at", "8:00 AM", "Store Open"),
        build("store.close_at", "11:30 PM", "Store Close"),
        build("store.working_hours", "8:00 AM - 11:30 PM", "Store Working Hours"),
        build("store.telegram_url", "", "Telegram URL"),
        build("store.location.latitude", "11.5564", "Store Latitude"),
        build("store.location.longitude", "104.9282", "Store Longitude"),

        // ── Payment ────────────────────────────────────────────────────────
        build("payment.bakong.merchant_id", "", "Bakong Merchant ID"),
        build("payment.bakong.merchant_name", "", "Bakong Merchant Name"),
        build("payment.bakong.city", "Phnom Penh", "Bakong City"),
        build("payment.bakong.enabled", "true", "Enable Bakong"),
        build("payment.stripe.publishable_key", "", "Stripe Publishable Key"),
        build("payment.stripe.enabled", "true", "Enable Stripe"),

        // ── Order ──────────────────────────────────────────────────────────
        build("order.auto_cancel_minutes", "30", "Auto-cancel Timeout (min)"),
        build("order.low_stock_threshold", "10", "Low Stock Threshold"),
        build("order.min_amount", "1", "Minimum Order Amount"),
        build("order.max_items", "50", "Max Items Per Order"),
        build("order.number_prefix", "ORD", "Order Number Prefix"),
        build("order.allow_guest_checkout", "false", "Allow Guest Checkout"),

        // ── Shipping ───────────────────────────────────────────────────────
        build("shipping.enabled", "true", "Enable Shipping"),
        build("shipping.flat_rate", "2.00", "Flat Shipping Rate"),
        build("shipping.free_threshold", "50.00", "Free Shipping Above"),
        build("shipping.label", "Delivery Fee", "Shipping Label"),

        // ── Tax ────────────────────────────────────────────────────────────
        build("tax.enabled", "false", "Enable Tax"),
        build("tax.rate", "10", "Tax Rate (%)"),

        // ── Notification ───────────────────────────────────────────────────
        build("notification.telegram.bot_token", "", "Telegram Bot Token"),
        build("notification.telegram.chat_id", "", "Telegram Chat ID"),

        // ── Media ──────────────────────────────────────────────────────────
        build("media.base_url", "", "Media Base URL"),
        build("media.max_file_size_mb", "5", "Max Upload Size (MB)"));
  }

  private Setting build(String key, String value, String label) {
    return Setting.builder().key(key).value(value).label(label).build();
  }
}
