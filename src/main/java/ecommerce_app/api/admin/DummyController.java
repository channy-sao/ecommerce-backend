package ecommerce_app.api.admin;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.dummy.DummyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/dummy")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class DummyController {
  // This controller is just a placeholder to demonstrate admin access control.
  // You can implement actual admin functionalities here in the future.

  private final DummyService dummyService;

  @GetMapping
  public ResponseEntity<String> dummyEndpoint() {
    return ResponseEntity.ok("This is a dummy endpoint for admin access control demonstration.");
  }

  @PostMapping
  public ResponseEntity<BaseBodyResponse> dummyData() {
    this.dummyService.dummyAll();
    return BaseBodyResponse.success(null, "Dummy data created successfully");
  }
}
