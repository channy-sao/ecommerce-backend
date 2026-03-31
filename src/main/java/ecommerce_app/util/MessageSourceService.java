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

  /** Get a message using the current locale from context */
  public String getMessage(String code) {
    return getMessage(code, null, LocaleContextHolder.getLocale());
  }

  /** Varargs — handles any number of arguments */
  public String getMessage(String code, Object... args) {
    return getMessage(code, args, LocaleContextHolder.getLocale());
  }

  /** Get a message with an explicit locale */
  public String getMessage(String code, Object[] args, Locale locale) {
    try {
      return messageSource.getMessage(code, args, locale);
    } catch (NoSuchMessageException e) {
      log.warn("Message not found: {} for locale: {}", code, locale);
      return code; // Return code as fallback
    }
  }

  /** Get a message with an explicit locale and no args */
  public String getMessage(String code, Locale locale) {
    return getMessage(code, null, locale);
  }

  /** Get a message with the default value if not found */
  public String getMessageOrDefault(String code, String defaultMessage) {
    return messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
  }

  /** Get a message with args and default value if not found */
  public String getMessageOrDefault(String code, String defaultMessage, Object... args) {
    try {
      return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    } catch (NoSuchMessageException e) {
      log.warn("Message not found: {} for locale: {}", code, LocaleContextHolder.getLocale());
      return defaultMessage;
    }
  }

  /** Check if a message exists */
  public boolean hasMessage(String code) {
    try {
      messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
      return true;
    } catch (NoSuchMessageException e) {
      return false;
    }
  }
}