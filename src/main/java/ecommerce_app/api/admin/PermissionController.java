package ecommerce_app.api.admin;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.user.service.PermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/admin/v1/permissions")
@Tag(name = "Permission Management", description = "For admin manage permissions")
public class PermissionController {
  private final PermissionService permissionService;

  @GetMapping
  public ResponseEntity<BaseBodyResponse> getPermissions() {
    return BaseBodyResponse.success(permissionService.getAllPermissions(), "Get all permissions");
  }
}
