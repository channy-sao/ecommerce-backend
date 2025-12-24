package ecommerce_app.modules.user.service;

import ecommerce_app.modules.user.model.entity.Permission;
import ecommerce_app.modules.user.model.entity.Role;

import java.util.Set;

public interface PermissionService {
    Set<Permission> getAllPermissions();
}
