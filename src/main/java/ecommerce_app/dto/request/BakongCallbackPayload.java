package ecommerce_app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BakongCallbackPayload {
  private String md5;
  private String transactionId;
  private String status;
  private String amount;
  private String currency;
  private String fromAccountId;
  private String toAccountId;
  private String timestamp;
}
