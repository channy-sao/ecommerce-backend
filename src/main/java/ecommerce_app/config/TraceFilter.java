package ecommerce_app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import static ecommerce_app.constant.app.AppConstant.REQUEST_ID;

@Component
public class TraceFilter extends OncePerRequestFilter {

  public static final String TRACE_ID = "traceId";
  public static final String PATH = "path";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // Get from header or generate
    String requestId = request.getHeader("X-Request-Id");
    if (requestId == null || requestId.isBlank()) {
      requestId = UUID.randomUUID().toString();
    }

    // Use same ID for trace & request
    String traceId = requestId;

    // Store in MDC
    MDC.put(REQUEST_ID, requestId);
    MDC.put(TRACE_ID, traceId);

    // Store in request attributes
    request.setAttribute(REQUEST_ID, requestId);
    request.setAttribute(TRACE_ID, traceId);
    request.setAttribute(PATH, request.getRequestURI());

    // Set in response headers
    response.setHeader("X-Request-Id", requestId);
    response.setHeader("X-Trace-Id", traceId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }
}
