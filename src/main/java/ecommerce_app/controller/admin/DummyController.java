package ecommerce_app.controller.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.service.impl.DummyService;
import ecommerce_app.util.MessageSourceService;
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
  private final MessageSourceService messageSourceService;

  @GetMapping
  public ResponseEntity<String> dummyEndpoint() {
    return ResponseEntity.ok(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping
  public ResponseEntity<BaseBodyResponse<Void>> dummyData() {
    this.dummyService.dummyAll();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/role")
  public ResponseEntity<BaseBodyResponse<Void>> dummyRoles() {
    this.dummyService.dummyRole();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/user")
  public ResponseEntity<BaseBodyResponse<Void>> dummyUsers() {
    this.dummyService.dummyUser();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/role-and-user")
  public ResponseEntity<BaseBodyResponse<Void>> dummyRolesAndUsers() {
    this.dummyService.dummyRoleAndUser();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/category")
  public ResponseEntity<BaseBodyResponse<Void>> dummyCategories() {
    this.dummyService.dummyCategory();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/product")
  public ResponseEntity<BaseBodyResponse<Void>> dummyProducts() {
    this.dummyService.dummyProduct();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/category-and-product")
  public ResponseEntity<BaseBodyResponse<Void>> dummyCategoriesAndProducts() {
    this.dummyService.dummyCategoryAndProduct();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/stock")
  public ResponseEntity<BaseBodyResponse<Void>> dummyStock() {
    this.dummyService.dummyStock();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/address")
  public ResponseEntity<BaseBodyResponse<Void>> dummyAddress() {
    this.dummyService.dummyAddress();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/cart")
  public ResponseEntity<BaseBodyResponse<Void>> dummyCart() {
    this.dummyService.dummyCart();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/order")
  public ResponseEntity<BaseBodyResponse<Void>> dummyOrder() {
    this.dummyService.dummyOrder();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }

  @PostMapping("/cart-and-order")
  public ResponseEntity<BaseBodyResponse<Void>> dummyCartAndOrder() {
    this.dummyService.dummyCardAndOrder();
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_SUCCESS));
  }
}
