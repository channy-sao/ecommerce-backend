package ecommerce_app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class TraceInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {

    // Generate a traceId (or use one from a header if provided)
    String traceId = UUID.randomUUID().toString();

    // Store in MDC (logging) and request attribute (for response)
    MDC.put("traceId", traceId);
    request.setAttribute("traceId", traceId);

    // Store request path
    request.setAttribute("path", request.getRequestURI());

    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    // Clean up MDC
    MDC.remove("traceId");
  }
}
