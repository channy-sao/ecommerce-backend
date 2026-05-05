package ecommerce_app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

  private String orderId;
  private String customerId;
  private BigDecimal amount;
  private String currency;

  private String purposeOfTransaction;
  private String mobileNumber;
  private Integer qrExpiryMinutes;
  private boolean generateDeepLink = false;
  private String appName;
  private String appIconUrl;
  private String appDeepLinkCallback;
}
