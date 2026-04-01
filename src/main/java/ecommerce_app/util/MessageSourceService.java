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

  /** No args — current locale */
  public String getMessage(String code) {
    return resolveMessage(code, null, LocaleContextHolder.getLocale());
  }

  /** Varargs — current locale */
  public String getMessage(String code, Object... args) {
    return resolveMessage(code, args, LocaleContextHolder.getLocale());
  }

  /** Default fallback — current locale */
  public String getMessageOrDefault(String code, String defaultMessage, Object... args) {
    try {
      return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    } catch (NoSuchMessageException e) {
      log.warn("Message not found: {} for locale: {}", code, LocaleContextHolder.getLocale());
      return defaultMessage;
    }
  }

  /** Default fallback — no args */
  public String getMessageOrDefault(String code, String defaultMessage) {
    return messageSource.getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
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

  // ── Private base method ───────────────────────────────────────────────────
  private String resolveMessage(String code, Object[] args, Locale locale) {
    try {
      return messageSource.getMessage(code, args, locale);
    } catch (NoSuchMessageException e) {
      log.warn("Message not found: {} for locale: {}", code, locale);
      return code; // fallback to key
    }
  }
}
