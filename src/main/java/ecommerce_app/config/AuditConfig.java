package ecommerce_app.config;

import ecommerce_app.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration class for enabling JPA auditing and setting up the auditor provider. This is used
 * to automatically populate the auditor fields (such as createdBy, lastModifiedBy) in entity
 * classes that are annotated with {@code @CreatedBy} and {@code @LastModifiedBy}.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableTransactionManagement
@RequiredArgsConstructor
public class AuditConfig {
  /**
   * Provides an implementation of {@link AuditorAware} that retrieves the current auditor (user).
   * This is typically used for tracking which user created or modified a given entity.
   *
   * @return an implementation of {@link AuditorAware} for auditing purposes.
   */
  @Bean
  public AuditorAware<Long> auditorProvider() {
    return new AuditorAwareImpl();
  }
}
