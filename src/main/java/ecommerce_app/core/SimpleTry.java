package ecommerce_app.core;

import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to handle checked exceptions in functional interfaces. Provides methods to execute
 * operations safely, either returning default values, rethrowing exceptions, or logging them
 * silently.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class SimpleTry {

  /**
   * Executes the given {@code CheckedSupplier}, handling any thrown exceptions. If an exception
   * occurs, logs the error, invokes the {@code exceptionHandler}, and returns {@code defaultValue}.
   *
   * @param supplier The supplier to execute.
   * @param defaultValue The default value to return in case of an exception.
   * @param exceptionHandler The consumer that processes exceptions.
   * @param <T> The type of the result.
   * @return The result of the supplier, or {@code defaultValue} if an exception occurs.
   */
  public static <T> T ofChecked(
      CheckedSupplier<? extends T> supplier, T defaultValue, Consumer<Throwable> exceptionHandler) {
    try {
      return supplier.get();
    } catch (Throwable throwable) {
      if (exceptionHandler != null) {
        log.error(throwable.getMessage(), throwable);
        exceptionHandler.accept(throwable);
      }
      return defaultValue;
    }
  }

  /**
   * Executes the given {@code CheckedSupplier} and returns its result. If an exception occurs,
   * returns {@code defaultValue} and wraps the exception in a {@link RuntimeException}.
   *
   * @param supplier The supplier to execute.
   * @param defaultValue The default value to return in case of an exception.
   * @param <T> The type of the result.
   * @return The result of the supplier, or {@code defaultValue} if an exception occurs.
   */
  public static <T> T ofChecked(CheckedSupplier<? extends T> supplier, T defaultValue) {
    return ofChecked(supplier, defaultValue, RuntimeException::new);
  }

  /**
   * Executes the given {@code CheckedSupplier}, rethrowing any exceptions via the provided
   * exception handler.
   *
   * @param supplier The supplier to execute.
   * @param exceptionHandler A function mapping the thrown exception to an instance of {@link
   *     Exception}.
   * @param <T> The type of the result.
   * @param <R> The type of the exception.
   * @return The result of the supplier.
   * @throws R If an exception occurs during execution.
   */
  public static <T, R extends Exception> T ofReThrowChecked(
      CheckedSupplier<? extends T> supplier, Function<Throwable, R> exceptionHandler) throws R {
    try {
      return supplier.get();
    } catch (Throwable throwable) {
      log.error(throwable.getMessage(), throwable);
      throw exceptionHandler.apply(throwable);
    }
  }

  /**
   * Executes the given {@code CheckedSupplier} and rethrows exceptions as {@link RuntimeException}.
   *
   * @param supplier The supplier to execute.
   * @param <T> The type of the result.
   * @return The result of the supplier.
   */
  public static <T> T ofReThrowChecked(CheckedSupplier<? extends T> supplier) {
    return ofReThrowChecked(supplier, RuntimeException::new);
  }

  /**
   * Executes the given {@code CheckedRunnable}, rethrowing any exceptions via the provided
   * exception handler.
   *
   * @param runnable The runnable to execute.
   * @param exceptionHandler A function mapping the thrown exception to an instance of {@link
   *     Exception}.
   * @param <R> The type of the exception.
   * @throws R If an exception occurs during execution.
   */
  public static <R extends Exception> void runReThrowChecked(
      CheckedRunnable runnable, Function<Throwable, R> exceptionHandler) throws R {
    try {
      runnable.run();
    } catch (Throwable throwable) {
      log.error(throwable.getMessage(), throwable);
      throw exceptionHandler.apply(throwable);
    }
  }

  /**
   * Executes the given {@code CheckedRunnable} and rethrows exceptions as {@link RuntimeException}.
   *
   * @param runnable The runnable to execute.
   */
  public static void runReThrowChecked(CheckedRunnable runnable) {
    runReThrowChecked(
        runnable,
        throwable -> {
          if (throwable instanceof RuntimeException exception) {
            return exception;
          }
          return new RuntimeException(throwable);
        });
  }

  /**
   * Executes the given {@code CheckedRunnable}, logging any exceptions but not rethrowing them.
   *
   * @param runnable The runnable to execute.
   */
  public static void runQuietly(CheckedRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable throwable) {
      log.error(throwable.getMessage(), throwable);
    }
  }
}
