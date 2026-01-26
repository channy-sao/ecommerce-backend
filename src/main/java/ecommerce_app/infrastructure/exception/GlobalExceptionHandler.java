package ecommerce_app.infrastructure.exception;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    log.error(ex.getMessage(), ex);
    return ResponseEntity.status(statusCode)
        .headers(headers)
        .body(BaseBodyResponse.bodyFailed(statusCode, ex.getMessage()));
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    log.error(ex.getMessage(), ex);
    String message =
        ex.getBindingResult().getAllErrors().stream()
            .map(ObjectError::getDefaultMessage)
            .collect(Collectors.joining("; "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .headers(headers)
        .body(BaseBodyResponse.bodyFailed(HttpStatus.BAD_REQUEST, message));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<BaseBodyResponse> handleBadRequestException(BadRequestException ex) {
    log.error(ex.getMessage(), ex);
    return BaseBodyResponse.failed(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<BaseBodyResponse> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    log.error(ex.getMessage(), ex);
    return BaseBodyResponse.failed(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<BaseBodyResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    log.error(ex.getMessage(), ex);
    return BaseBodyResponse.failed(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(TransactionSystemException.class)
  public ResponseEntity<BaseBodyResponse> handleTransactionSystemException(
      TransactionSystemException ex) {
    log.error(ex.getMessage(), ex);
    return BaseBodyResponse.failed(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<BaseBodyResponse> handleIllegalStateException(IllegalStateException ex) {
    log.error(ex.getMessage(), ex);
    return BaseBodyResponse.failed(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }
}
