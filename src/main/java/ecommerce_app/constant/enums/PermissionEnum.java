package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PermissionEnum {
  // ===== USER =====
  USER_READ("USER_READ", "Read user information"),
  USER_CREATE("USER_CREATE", "Create new user"),
  USER_UPDATE("USER_UPDATE", "Update user information"),
  USER_DELETE("USER_DELETE", "Delete user"),

  // ===== ROLE =====
  ROLE_READ("ROLE_READ", "Read role information"),
  ROLE_CREATE("ROLE_CREATE", "Create new role"),
  ROLE_UPDATE("ROLE_UPDATE", "Update role information"),
  ROLE_DELETE("ROLE_DELETE", "Delete role"),

  // ===== PRODUCT =====
  PRODUCT_READ("PRODUCT_READ", "Read product information"),
  PRODUCT_CREATE("PRODUCT_CREATE", "Create new product"),
  PRODUCT_UPDATE("PRODUCT_UPDATE", "Update product information"),
  PRODUCT_DELETE("PRODUCT_DELETE", "Delete product"),

  // ===== CATEGORY =====
  CATEGORY_READ("CATEGORY_READ", "Read category information"),
  CATEGORY_CREATE("CATEGORY_CREATE", "Create new category"),
  CATEGORY_UPDATE("CATEGORY_UPDATE", "Update category information"),
  CATEGORY_DELETE("CATEGORY_DELETE", "Delete category"),

  // add news below

  // ===== ORDER =====
  ORDER_CANCEL("ORDER_CANCEL", "Cancel order information"),
  ORDER_CREATE("ORDER_CREATE", "Create new order"),
  ORDER_UPDATE("ORDER_UPDATE", "Update order information"),
  ORDER_DELETE("ORDER_DELETE", "Delete order"),

  // ===== ADDRESS =====
  ADDRESS_READ("ADDRESS_READ", "Read address information"),
  ADDRESS_CREATE("ADDRESS_CREATE", "Create new address information"),
  ADDRESS_UPDATE("ADDRESS_UPDATE", "Update address information"),
  ADDRESS_DELETE("ADDRESS_DELETE", "Delete address information");

  private final String name;
  private final String description;

  public String getCategory() {
    return this.name().split("_")[0];
  }
}
