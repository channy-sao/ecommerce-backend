package ecommerce_app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitiatePaymentResponse {

  private Long paymentId;
  private PaymentGateway gateway;
  private PaymentStatus status;
  private BigDecimal amount;
  private String currency;
  private LocalDateTime expiredAt;
  private String message;

  // BAKONG only
  private String bakongDeeplink;

  // STRIPE only (future)
  private String stripeClientSecret;
}
