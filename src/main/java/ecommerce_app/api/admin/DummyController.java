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
  public ResponseEntity<BaseBodyResponse<Void>> dummyData() {
    this.dummyService.dummyAll();
    return BaseBodyResponse.success("Dummy all data created successfully");
  }

  @PostMapping("/role")
  public ResponseEntity<BaseBodyResponse<Void>> dummyRoles() {
    this.dummyService.dummyRole();
    return BaseBodyResponse.success("Dummy roles data created successfully");
  }

  @PostMapping("/user")
  public ResponseEntity<BaseBodyResponse<Void>> dummyUsers() {
    this.dummyService.dummyUser();
    return BaseBodyResponse.success("Dummy users data created successfully");
  }

  @PostMapping("/role-and-user")
  public ResponseEntity<BaseBodyResponse<Void>> dummyRolesAndUsers() {
    this.dummyService.dummyRoleAndUser();
    return BaseBodyResponse.success("Dummy role & user data created successfully");
  }

  @PostMapping("/category")
  public ResponseEntity<BaseBodyResponse<Void>> dummyCategories() {
    this.dummyService.dummyCategory();
    return BaseBodyResponse.success("Dummy category data created successfully");
  }

  @PostMapping("/product")
  public ResponseEntity<BaseBodyResponse<Void>> dummyProducts() {
    this.dummyService.dummyProduct();
    return BaseBodyResponse.success("Dummy product data created successfully");
  }

  @PostMapping("/category-and-product")
  public ResponseEntity<BaseBodyResponse<Void>> dummyCategoriesAndProducts() {
    this.dummyService.dummyCategoryAndProduct();
    return BaseBodyResponse.success("Dummy category & product data created successfully");
  }

  @PostMapping("/stock")
  public ResponseEntity<BaseBodyResponse<Void>> dummyStock() {
    this.dummyService.dummyStock();
    return BaseBodyResponse.success("Dummy stock data created successfully");
  }

  @PostMapping("/address")
  public ResponseEntity<BaseBodyResponse<Void>> dummyAddress() {
    this.dummyService.dummyAddress();
    return BaseBodyResponse.success("Dummy address data created successfully");
  }

  @PostMapping("/cart")
  public ResponseEntity<BaseBodyResponse<Void>> dummyCart() {
    this.dummyService.dummyCart();
    return BaseBodyResponse.success("Dummy cart data created successfully");
  }

  @PostMapping("/order")
  public ResponseEntity<BaseBodyResponse<Void>> dummyOrder() {
    this.dummyService.dummyOrder();
    return BaseBodyResponse.success("Dummy order process data created successfully");
  }

  @PostMapping("/cart-and-order")
  public ResponseEntity<BaseBodyResponse<Void>> dummyCartAndOrder() {
    this.dummyService.dummyCardAndOrder();
    return BaseBodyResponse.success("Dummy all data created successfully");
  }
}
