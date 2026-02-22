package ecommerce_app.exception;

public class SimpleTryException extends RuntimeException {
  public SimpleTryException(String message) {
    super(message);
  }

  public SimpleTryException(Throwable cause) {
    super(cause);
  }

  public SimpleTryException(String message, Throwable cause) {
    super(message, cause);
  }
}
