package ecommerce_app.infrastructure.exception;

import java.util.List;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

  public ResourceNotFoundException(String resourceName, Long id) {
    super(HttpStatus.NOT_FOUND, String.format("%s with id %d is not found", resourceName, id));
  }

  public ResourceNotFoundException(String resourceName, String uuid) {
    super(HttpStatus.NOT_FOUND, String.format("%s with uuid %s is not found", resourceName, uuid));
  }

  public ResourceNotFoundException(String message, List<Long> idList) {
    super(HttpStatus.NOT_FOUND, String.format("%s is not found with ids %s", message, idList));
  }

  public ResourceNotFoundException(String message){
    super(HttpStatus.NOT_FOUND, message);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(HttpStatus.NOT_FOUND, String.format("%s is not found", message));
  }
}
