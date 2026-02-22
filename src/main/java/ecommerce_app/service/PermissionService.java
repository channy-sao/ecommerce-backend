package ecommerce_app.service;

import ecommerce_app.entity.Permission;

import java.util.Set;

public interface PermissionService {
    Set<Permission> getAllPermissions();
}
