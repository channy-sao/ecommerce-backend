package ecommerce_app.controller.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.entity.Permission;
import ecommerce_app.service.PermissionService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
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
  private final MessageSourceService messageSourceService;

  @GetMapping
  public ResponseEntity<BaseBodyResponse<Set<Permission>>> getPermissions() {
    return BaseBodyResponse.success(
        permissionService.getAllPermissions(),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
