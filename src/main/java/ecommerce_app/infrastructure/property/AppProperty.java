package ecommerce_app.infrastructure.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
@ConfigurationProperties(prefix = "app")
@Component
@Lazy
@Getter
@Setter

public class AppProperty {
  private String baseUrl;
  private String appName;
  private String appDescription;
  private String appVersion;
  private String appOriginUrl;
  private String secretKey;
  private Jwt jwt;


  @Getter
  @Setter
  public static class Jwt{
      private String secretKey;
      private Long accessExpiredInMinute;
      private Long refreshExpiredInMinute;
  }
}