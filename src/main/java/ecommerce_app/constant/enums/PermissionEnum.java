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
  ADDRESS_DELETE("ADDRESS_DELETE", "Delete address information"),

  // ===== REVIEW =====
  REVIEW_READ("REVIEW_READ", "Read review information"),
  REVIEW_CREATE("REVIEW_CREATE", "Create new review information"),
  REVIEW_UPDATE("REVIEW_UPDATE", "Update review information"),
  REVIEW_DELETE("REVIEW_DELETE", "Delete review information"),

  // ===== PROMOTION =====
  PROMOTION_READ("PROMOTION_READ", "Read promotion information"),
  PROMOTION_CREATE("PROMOTION_CREATE", "Create new promotion information"),
  PROMOTION_UPDATE("PROMOTION_UPDATE", "Update promotion information"),
  PROMOTION_DELETE("PROMOTION_DELETE", "Delete promotion information"),

  // ===== STOCK =====
  STOCK_READ("STOCK_READ", "Read stock information"),
  STOCK_CREATE("STOCK_CREATE", "Create new stock information"),
  STOCK_UPDATE("STOCK_UPDATE", "Update stock information"),
  STOCK_DELETE("STOCK_DELETE", "Delete stock information"),

  // ===== REPORT =====
  REPORT_READ("REPORT_READ", "Read report information"),
  REPORT_CREATE("REPORT_CREATE", "Create new report information"),
  REPORT_UPDATE("REPORT_UPDATE", "Update report information"),
  REPORT_DELETE("REPORT_DELETE", "Delete report information"),

  // ===== DASHBOARD =====
  DASHBOARD_READ("DASHBOARD_READ", "Read dashboard information"),
  DASHBOARD_UPDATE("DASHBOARD_UPDATE", "Update dashboard information"),

  // ===== STOCK_IMPORT =====
  STOCK_IMPORT("STOCK_IMPORT", "Import stock information"),

  // ===== ANALYTICS =====
  ANALYTICS_READ("ANALYTICS_READ", "Read analytics information"),
  ANALYTICS_UPDATE("ANALYTICS_UPDATE", "Update analytics information"),

  // ===== SETTINGS =====
  SETTINGS_READ("SETTINGS_READ", "Read settings information"),
  SETTINGS_UPDATE("SETTINGS_UPDATE", "Update settings information"),

  // ===== NOTIFICATION =====
  NOTIFICATION_READ("NOTIFICATION_READ", "Read notification information"),
  NOTIFICATION_CREATE("NOTIFICATION_CREATE", "Create new notification information"),
  NOTIFICATION_UPDATE("NOTIFICATION_UPDATE", "Update notification information"),
  NOTIFICATION_DELETE("NOTIFICATION_DELETE", "Delete notification information"),

  // ===== MEDIA =====
  MEDIA_READ("MEDIA_READ", "Read media information"),
  MEDIA_CREATE("MEDIA_CREATE", "Create new media information"),
  MEDIA_UPDATE("MEDIA_UPDATE", "Update media information"),
  MEDIA_DELETE("MEDIA_DELETE", "Delete media information"),

  // ===== OTHER =====
  OTHER_READ("OTHER_READ", "Read other information"),
  OTHER_CREATE("OTHER_CREATE", "Create new other information"),
  OTHER_UPDATE("OTHER_UPDATE", "Update other information"),
  OTHER_DELETE("OTHER_DELETE", "Delete other information");

  private final String name;
  private final String description;

  public String getCategory() {
    return this.name().split("_")[0];
  }
}
