package ecommerce_app.exception;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.util.MessageSourceService;
import lombok.RequiredArgsConstructor;
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
        ex.getBindingResult().getAllErrors().stream()
            .map(ObjectError::getDefaultMessage)
            .collect(Collectors.joining("; "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .headers(headers)
        .body(BaseBodyResponse.bodyFailed(HttpStatus.BAD_REQUEST, message));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<BaseBodyResponse<Void>> handleBadRequestException(BadRequestException ex) {
    log.error(ex.getMessage(), ex);
    String message =
        ex.getMessage() != null
            ? ex.getMessage()
            : messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_ERROR);
    return BaseBodyResponse.failed(HttpStatus.BAD_REQUEST, message);
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
}
