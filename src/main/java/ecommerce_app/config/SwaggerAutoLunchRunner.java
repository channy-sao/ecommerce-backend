package ecommerce_app.config;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SwaggerAutoLunchRunner {
  private static final String SWAGGER_URL = "http://localhost:8080/swagger-ui/index.html";
  @EventListener(ApplicationReadyEvent.class)
  public void lunch() throws IOException {

    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      // Windows
      new ProcessBuilder("cmd", "/c", "start", "chrome", SWAGGER_URL).start();
      log.info("Swagger started successfully in Windows chrome");
    } else if (os.contains("mac")) {
      // macOS
      new ProcessBuilder("open", "-a", "Google Chrome", SWAGGER_URL).start();
      log.info("Swagger started successfully in Mac OS X chrome");
    } else if (os.contains("nix") || os.contains("nux")) {
      // Linux
      new ProcessBuilder("google-chrome", SWAGGER_URL).start();
      log.info("Swagger started successfully in Linux chrome");
    } else {
      log.error("Unknown OS: Open manually at " + SWAGGER_URL);
    }
  }
}
