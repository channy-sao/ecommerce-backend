package ecommerce_app.api.admin;

import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.dummy.service.DummyService;
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
    return BaseBodyResponse.success(null, "Dummy all data created successfully");
  }

  @PostMapping("/role")
  public ResponseEntity<BaseBodyResponse> dummyRoles() {
    this.dummyService.dummyRole();
    return BaseBodyResponse.success(null, "Dummy roles data created successfully");
  }

  @PostMapping("/user")
  public ResponseEntity<BaseBodyResponse> dummyUsers() {
    this.dummyService.dummyUser();
    return BaseBodyResponse.success(null, "Dummy users data created successfully");
  }

  @PostMapping("/role-and-user")
  public ResponseEntity<BaseBodyResponse> dummyRolesAndUsers() {
    this.dummyService.dummyRoleAndUser();
    return BaseBodyResponse.success(null, "Dummy role & user data created successfully");
  }

  @PostMapping("/category")
  public ResponseEntity<BaseBodyResponse> dummyCategories() {
    this.dummyService.dummyCategory();
    return BaseBodyResponse.success(null, "Dummy category data created successfully");
  }

  @PostMapping("/product")
  public ResponseEntity<BaseBodyResponse> dummyProducts() {
    this.dummyService.dummyProduct();
    return BaseBodyResponse.success(null, "Dummy product data created successfully");
  }

  @PostMapping("/category-and-product")
  public ResponseEntity<BaseBodyResponse> dummyCategoriesAndProducts() {
    this.dummyService.dummyCategoryAndProduct();
    return BaseBodyResponse.success(null, "Dummy category & product data created successfully");
  }

  @PostMapping("/stock")
  public ResponseEntity<BaseBodyResponse> dummyStock() {
    this.dummyService.dummyStock();
    return BaseBodyResponse.success(null, "Dummy stock data created successfully");
  }

  @PostMapping("/address")
  public ResponseEntity<BaseBodyResponse> dummyAddress() {
    this.dummyService.dummyAddress();
    return BaseBodyResponse.success(null, "Dummy address data created successfully");
  }

  @PostMapping("/cart")
  public ResponseEntity<BaseBodyResponse> dummyCart() {
    this.dummyService.dummyCart();
    return BaseBodyResponse.success(null, "Dummy cart data created successfully");
  }

  @PostMapping("/order")
  public ResponseEntity<BaseBodyResponse> dummyOrder() {
    this.dummyService.dummyOrder();
    return BaseBodyResponse.success(null, "Dummy order process data created successfully");
  }

  @PostMapping("/cart-and-order")
  public ResponseEntity<BaseBodyResponse> dummyCartAndOrder() {
    this.dummyService.dummyCardAndOrder();
    return BaseBodyResponse.success(null, "Dummy all data created successfully");
  }
}
