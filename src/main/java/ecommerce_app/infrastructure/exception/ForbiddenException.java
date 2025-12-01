package ecommerce_app.infrastructure.exception;

import org.springframework.security.access.AccessDeniedException;

public class ForbiddenException extends AccessDeniedException {
  public ForbiddenException(String message) {
    super(message);
  }
}
