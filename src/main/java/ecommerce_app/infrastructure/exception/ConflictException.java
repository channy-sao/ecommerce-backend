package ecommerce_app.infrastructure.exception;

public class ConflictException extends BaseException {
  public ConflictException(String msg) {
    super(msg);
  }
}