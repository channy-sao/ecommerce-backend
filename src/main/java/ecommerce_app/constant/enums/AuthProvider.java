package ecommerce_app.constant.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthProvider {
  LOCAL("local"),
  EMAIL_PASSWORD("password"),
  GOOGLE("google.com"),
  GITHUB("github.com"),
  FACEBOOK("facebook.com"),
  APPLE("apple.com"),
  PHONE_NUMBER("phone"),
  ANONYMOUS("anonymous");
  private final String provider;

  public static AuthProvider fromProviderString(String providerString) {
    for (AuthProvider pd : AuthProvider.values()) {
      if (pd.getProvider().equalsIgnoreCase(providerString)) {
        return pd;
      }
    }
    throw new IllegalArgumentException("Unknown provider: " + providerString);
  }
}
