package ecommerce_app.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageUtils {

  private final MessageSource messageSource;

  /** Get translated message using current request locale. */
  public String getMessage(String key) {
    return getMessage(key, null, LocaleContextHolder.getLocale());
  }

  /** Get translated message with parameters using current locale. */
  public String getMessage(String key, Object[] args) {
    return getMessage(key, args, LocaleContextHolder.getLocale());
  }

  /** Get translated message with specific locale. */
  public String getMessage(String key, Object[] args, Locale locale) {
    return messageSource.getMessage(
        key, args, key, // fallback to key if not found
        locale);
  }
}
