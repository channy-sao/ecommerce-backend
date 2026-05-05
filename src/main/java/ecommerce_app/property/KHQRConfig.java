package ecommerce_app.property;

import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "khqr")
@Getter
@Setter
public class KHQRConfig {
  private String bakongAccountId; // e.g. "yourshop@devb"
  private String merchantId; // From NBC / your bank
  private String acquiringBank; // e.g. "ABA Bank"
  private String merchantName; // e.g. "My Shop"
  private String merchantCity; // e.g. "PHNOM PENH"
  private String merchantCategoryCode; // MCC code e.g. "5999"
  private String merchantNameKhmer;
  private String merchantCityKhmer;
  private int defaultExpiryMinutes = 15;
  private String deepLinkApiUrl;
  private KHQRCurrency currency = KHQRCurrency.USD;
  private String merchantSecret;

  private String apiBaseUrl;
  private String apiKey;
  private String apiSecret;
}
