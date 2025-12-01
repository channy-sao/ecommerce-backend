package ecommerce_app.constant.app;

import ecommerce_app.util.StaticBeanUtils;

public final class AppConstant {
  // Private constructor to prevent instantiation
  private AppConstant() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String APP_NAME = StaticBeanUtils.getAppProperty().getAppName();
  public static final String APP_DESCRIPTION = StaticBeanUtils.getAppProperty().getAppDescription();
  public static final String APP_VERSION = StaticBeanUtils.getAppProperty().getAppVersion();
  public static final String APP_ORIGIN_URL = StaticBeanUtils.getAppProperty().getAppOriginUrl();

  // JWT
  public static final String JWT_SCHEME_NAME = "bearerAuth";
  public static final String JWT_BEARER_FORMAT = "JWT";
  public static final String JWT_SCHEME = "bearer";

  // MEDIA TYPE
  public static final String APPLICATION_JSON = "application/json";

  // KEYCLOAK
  public static final String KEYCLOAK_SCHEME_NAME = "keycloak";
}