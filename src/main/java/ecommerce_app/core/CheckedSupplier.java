package ecommerce_app.core;


public interface CheckedSupplier<T> {
  @SuppressWarnings("java:S112")
  T get() throws Throwable;
}
