package ecommerce_app.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
public class MessageSourceService {

  private final MessageSource messageSource;

  public MessageSourceService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  /** Get message using current locale from context */
  public String getMessage(String code) {
    return getMessage(code, null, LocaleContextHolder.getLocale());
  }

  /** Get message with arguments using current locale */
  public String getMessage(String code, Object[] args) {
    return getMessage(code, args, LocaleContextHolder.getLocale());
  }

  /** Get message with explicit locale */
  public String getMessage(String code, Object[] args, Locale locale) {
    try {
      return messageSource.getMessage(code, args, locale);
    } catch (NoSuchMessageException e) {
      log.warn("Message not found: {} for locale: {}", code, locale);
      return code; // Return code as fallback
    }
  }

  /** Get message with default value if not found */
  public String getMessageOrDefault(String code, String defaultMessage) {
    return messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
  }

  /** Check if message exists */
  public boolean hasMessage(String code) {
    try {
      messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
      return true;
    } catch (NoSuchMessageException e) {
      return false;
    }
  }
}
