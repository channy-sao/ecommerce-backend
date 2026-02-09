// config/RequestLoggingInterceptor.java
package ecommerce_app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
    log.info("═══════════════════════════════════════");
    log.info("📥 INCOMING REQUEST TO SPRING BOOT");
    log.info("URL: {}", request.getRequestURL());
    log.info("Method: {}", request.getMethod());

    log.info("📋 ALL HEADERS:");
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      String headerValue = request.getHeader(headerName);
      log.info("  {}: {}", headerName, headerValue);
    }

    log.info("🌐 Accept-Language: {}", request.getHeader("Accept-Language"));
    log.info("═══════════════════════════════════════");

    return true;
  }
}
