package ecommerce_app.infrastructure.exception;

public class BadRequestException extends BaseException {
  public BadRequestException(String msg) {
    super(msg);
  }
}
