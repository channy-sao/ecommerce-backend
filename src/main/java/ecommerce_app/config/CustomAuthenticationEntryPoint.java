package ecommerce_app.config;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.util.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    HttpStatusCode httpStatusCode = HttpStatusCode.valueOf(401);

    response.setStatus(httpStatusCode.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    PrintWriter writer = response.getWriter();
    writer.print(
        JsonUtils.toJson(BaseBodyResponse.bodyFailed(httpStatusCode, authException.getMessage())));
    writer.flush();
    writer.close();
  }
}
