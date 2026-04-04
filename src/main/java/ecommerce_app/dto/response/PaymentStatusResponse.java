package ecommerce_app.dto.response;

import ecommerce_app.constant.enums.PaymentGateway;
import ecommerce_app.constant.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatusResponse {

  private Long paymentId;
  private Long orderId;
  private PaymentGateway gateway;
  private PaymentStatus status;
  private BigDecimal amount;
  private String currency;
  private LocalDateTime paidAt;
  private String message;
}
