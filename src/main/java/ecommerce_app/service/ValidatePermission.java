package ecommerce_app.service;

import java.util.Set;

public abstract class ValidatePermission {
  protected abstract void validatePermission(Set<Long> permissionIds);
}
