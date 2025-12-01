package ecommerce_app.util;

import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

public final class ApplicationContextUtils {
  @Setter private static ApplicationContext context;

  private ApplicationContextUtils() {}

  public static ApplicationContext getContext() {
    if (context == null) throw new ApplicationContextException("Application context load failed");
    return context;
  }
}