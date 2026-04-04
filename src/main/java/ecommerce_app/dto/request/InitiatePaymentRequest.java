package ecommerce_app.dto.request;

import ecommerce_app.constant.enums.PaymentGateway;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InitiatePaymentRequest {

  @NotNull(message = "Order ID is required")
  private Long orderId;

  @NotNull(message = "Payment gateway is required")
  private PaymentGateway gateway;

  // Optional: currency override (defaults to USD if null)
  private String currency;
}
