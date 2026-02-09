package ecommerce_app.config;

// ============================================================================
// PRODUCTION-READY I18N CONFIGURATION - BEST PRACTICES
// ============================================================================

import ecommerce_app.infrastructure.property.I18nProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(I18nProperties.class)
public class I18nConfig implements WebMvcConfigurer {

  private final I18nProperties i18nProperties;
  private final RequestLoggingInterceptor requestLoggingInterceptor;

  public I18nConfig(
      I18nProperties i18nProperties, RequestLoggingInterceptor requestLoggingInterceptor) {
    this.i18nProperties = i18nProperties;
    this.requestLoggingInterceptor = requestLoggingInterceptor;
  }

  /**
   * LocaleResolver using Accept-Language header (REST API best practice) Falls back to default
   * locale if header is missing or invalid
   */
  @Bean
  public LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
    resolver.setSupportedLocales(i18nProperties.getSupportedLocales());
    resolver.setDefaultLocale(i18nProperties.getDefaultLocale());
    return resolver;
  }

  /** Allows locale override via query parameter (useful for testing) Example: ?lang=es */
  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
    interceptor.setParamName(i18nProperties.getParamName());
    interceptor.setIgnoreInvalidLocale(true);
    return interceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localeChangeInterceptor());
    registry.addInterceptor(requestLoggingInterceptor);
  }

  /**
   * MessageSource with production-ready configuration - UTF-8 encoding for international characters
   * - Caching for performance - Proper fallback behavior
   */
  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasenames(i18nProperties.getBasename());
    messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
    messageSource.setFallbackToSystemLocale(false);
    messageSource.setUseCodeAsDefaultMessage(false);
    messageSource.setAlwaysUseMessageFormat(false);

    // Cache messages in production, disable in dev for hot reload
    if (i18nProperties.getCacheSeconds() != null) {
      messageSource.setCacheSeconds(i18nProperties.getCacheSeconds());
    }

    return messageSource;
  }

  /** Integrates MessageSource with Bean Validation Allows i18n validation messages */
  @Bean
  public LocalValidatorFactoryBean validator(MessageSource messageSource) {
    LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
    bean.setValidationMessageSource(messageSource);
    return bean;
  }
}
