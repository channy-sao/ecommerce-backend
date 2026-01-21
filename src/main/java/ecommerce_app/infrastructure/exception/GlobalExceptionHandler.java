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
    return BaseBodyResponse.internalFailed(statusCode, ex.getMessage());
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    String message = "Invalid value";

    for (ObjectError err : ex.getBindingResult().getAllErrors()) {
      message = err.getDefaultMessage();
    }

    return BaseBodyResponse.internalFailed(httpStatus, message);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<BaseBodyResponse> handleBadRequestException(BadRequestException ex) {
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    return BaseBodyResponse.failed(httpStatus, ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<BaseBodyResponse> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    return BaseBodyResponse.failed(httpStatus, ex.getMessage());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<BaseBodyResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    HttpStatus httpStatus = HttpStatus.NOT_FOUND;
    return BaseBodyResponse.failed(httpStatus, ex.getMessage());
  }

  @ExceptionHandler(TransactionSystemException.class)
  public ResponseEntity<BaseBodyResponse> handleTransactionSystemException(
      TransactionSystemException ex) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    return BaseBodyResponse.failed(httpStatus, ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<BaseBodyResponse> handleIllegalStateException(IllegalStateException ex) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    return BaseBodyResponse.failed(httpStatus, ex.getMessage());
  }
}
