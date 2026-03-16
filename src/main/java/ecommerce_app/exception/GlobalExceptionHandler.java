package ecommerce_app.exception;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.util.MessageSourceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  private final MessageSourceService messageSourceService;

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
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
            .orElse("Validation error");

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .headers(headers)
        .body(BaseBodyResponse.bodyFailed(HttpStatus.BAD_REQUEST, message));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
    log.error(ex.getMessage(), ex);
    String message = ex.getConstraintViolations()
            .stream()
            .findFirst()
            .map(ConstraintViolation::getMessage)  // ← returns clean message only, no path prefix
            .orElse("Validation error");

    return BaseBodyResponse.failed(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleBadRequestException(BadRequestException ex) {
    log.error(ex.getMessage(), ex);
    return BaseBodyResponse.failed(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    log.error(ex.getMessage(), ex);
    return BaseBodyResponse.failed(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    log.error(ex.getMessage(), ex);
    String message = messageSourceService.getMessage(MessageKeyConstant.ERROR_MESSAGE_404);
    return BaseBodyResponse.failed(HttpStatus.NOT_FOUND, message);
  }

  @ExceptionHandler(TransactionSystemException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleTransactionSystemException(
      TransactionSystemException ex) {
    log.error(ex.getMessage(), ex);
    return BaseBodyResponse.failed(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleIllegalStateException(
      IllegalStateException ex) {
    log.error(ex.getMessage(), ex);
    return BaseBodyResponse.failed(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  @ExceptionHandler(InternalServerErrorException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleInternalServerErrorException(
      InternalServerErrorException ex) {
    log.error(ex.getMessage(), ex);
    String message =
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SERVER_ERROR);

    return BaseBodyResponse.failed(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleBusinessException(BusinessException ex) {
    log.error(ex.getMessage(), ex);
    String message = messageSourceService.getMessage(ex.getMessage());
    return BaseBodyResponse.failed(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleDuplicate(
      DuplicateResourceException ex, HttpServletRequest request) {

    return BaseBodyResponse.failed(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleNotFound(
      EntityNotFoundException ex, HttpServletRequest request) {
    return BaseBodyResponse.failed(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  // --- Database ---

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleDataIntegrity(
      DataIntegrityViolationException ex, HttpServletRequest request) {

    String message = "Database constraint violation";
    String causeMsg = ex.getMostSpecificCause().getMessage().toLowerCase();

    if (causeMsg.contains("duplicate key") || causeMsg.contains("unique constraint")) {
      message = "Resource already exists";
    } else if (causeMsg.contains("foreign key")) {
      message = "Referenced resource does not exist";
    } else if (causeMsg.contains("null value in column")) {
      message = "Required field is missing";
    } else if (causeMsg.contains("check constraint")) {
      message = "Field value is out of allowed range";
    } else if (causeMsg.contains("value too long")) {
      message = "Field value exceeds maximum length";
    }

    return BaseBodyResponse.failed(HttpStatus.CONFLICT, message);
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleOptimisticLock(
      OptimisticLockingFailureException ex, HttpServletRequest request) {
    return BaseBodyResponse.failed(
        HttpStatus.CONFLICT, "Resource was modified by another request, please retry");
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleDataAccess(
      DataAccessException ex, HttpServletRequest request) {
    return BaseBodyResponse.failed(
        HttpStatus.SERVICE_UNAVAILABLE, "Database is temporarily unavailable");
  }
}
