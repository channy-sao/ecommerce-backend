package ecommerce_app.infrastructure.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.storage.upload")
@Component
@Lazy
@Getter
@Setter
public class StorageConfigProperty {
  private String avatar;
  private String product;
}
