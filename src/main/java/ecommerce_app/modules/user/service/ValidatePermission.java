package ecommerce_app.modules.user.service;

import java.util.List;
import java.util.Set;

public abstract class ValidatePermission {
    protected abstract void validatePermission(Set<Long> permissionIds);
}
