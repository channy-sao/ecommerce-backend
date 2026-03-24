package ecommerce_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ping")
public class PingController {
  /** Simple lightweight ping endpoint for frontend health check. No authentication required. */
  @GetMapping
  public String ping() {
    return "Service is up and running!"; // Simple and fast
  }
}
