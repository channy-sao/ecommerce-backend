package ecommerce_app.util;

import ecommerce_app.infrastructure.property.AppProperty;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utility class that provides static access to beans from the Spring application context. This
 * class helps in retrieving application properties, password encoders, and current user details
 * without directly injecting the dependencies into each class.
 *
 * <p>The methods in this class allow for easy access to specific Spring beans throughout the
 * application.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class StaticBeanUtils {

  // Retrieve the application context (assumed to be set via ApplicationContextUtil)
  private static final ApplicationContext context = ApplicationContextUtils.getContext();

  /**
   * Retrieves the application properties (AppProperty bean) from the Spring context.
   *
   * @return An instance of AppProperty that contains the application's configuration.
   * @see AppProperty
   */
  public static AppProperty getAppProperty() {
    return context.getBean(AppProperty.class);
  }

  /**
   * Retrieves the PasswordEncoder bean from the Spring context. This is typically used for encoding
   * or decoding user passwords.
   *
   * @return An instance of PasswordEncoder to handle password encryption.
   * @see PasswordEncoder
   */
  public static PasswordEncoder getPasswordEncoder() {
    return context.getBean(PasswordEncoder.class);
  }

  /**
   * Retrieves the current user authentication details (UserAuthDetailsService) from the Spring
   * context. Returns an Optional of UserAuthDetails to handle situations where the user might not
   * be authenticated.
   *
   * @return An Optional containing the current UserAuthDetails or an empty Optional if no user is
   *     authenticated.
   * @see UserAuthDetails
   * @see UserAuthDetailsService
   */
  //    public static Optional<UserAuthDetails> getCurrentUserOrNull() {
  //        return context.getBean(UserAuthDetailsService.class).getCurrentUserOrNull();
  //    }
}