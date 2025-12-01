package ecommerce_app.infrastructure.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class ApiException extends RuntimeException {
  private final HttpStatus status;
  private final String message;
}
